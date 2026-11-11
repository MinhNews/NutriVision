"""Tai 1 anh dai dien cho moi mon trong danh muc NutriVision.

Nguon:
  - 131 lop model: HuggingFace datasets-server (rows API), 1 anh/lop.
  - 14 mon khong co lop model: Wikimedia Commons (giay phep CC/PD).

Khong tai ca dataset. Anh duoc thu nho, chuyen WebP, luu vao
app/src/main/assets/food_images/<food_id>.webp

Script co the chay lai nhieu lan: file da co se duoc bo qua (resume),
offset tung lop duoc cache trong ai_tools/_source/hf_offsets.json.
Co throttle + exponential backoff khi gap HTTP 429.
"""
import io
import json
import os
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

from PIL import Image, ImageFilter, ImageStat

sys.stdout.reconfigure(encoding="utf-8")

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
OUT_DIR = os.path.join(ASSETS, "food_images")
SOURCE_DIR = os.path.join(ROOT, "ai_tools", "_source")
OFFSET_CACHE = os.path.join(SOURCE_DIR, "hf_offsets.json")
os.makedirs(OUT_DIR, exist_ok=True)

UA = "NutriVisionStudentProject/1.0 (educational research; contact: student@example.com)"
VN_DATASET = "jeandeanny/vn-food-combined-2026"
F101_DATASET = "ethz/food101"

REQUEST_GAP = 1.6          # giay giua 2 request HuggingFace
MAX_RETRIES = 6
BACKOFF_BASE = 8           # 8s, 16s, 32s, 64s, 128s, 256s


def load_json(path):
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


with open(os.path.join(ASSETS, "foods_nutrition.json"), encoding="utf-8") as fh:
    FOODS = json.load(fh)
CLASS_MAP = load_json(os.path.join(SOURCE_DIR, "class_map.json"))

MODEL_FOODS = {f["id"] for f in FOODS if f.get("model_class")}
VN_MAP = CLASS_MAP["vietnamese"]       # hf label -> food id
F101_MAP = CLASS_MAP["international"]  # hf label -> food id

_last_request = 0.0


def _throttle():
    global _last_request
    wait = REQUEST_GAP - (time.time() - _last_request)
    if wait > 0:
        time.sleep(wait)
    _last_request = time.time()


def http_get(url, timeout=90, retries=MAX_RETRIES):
    """GET co throttle + exponential backoff cho 429/5xx."""
    for attempt in range(retries):
        _throttle()
        req = urllib.request.Request(url, headers={
            "User-Agent": UA,
            "Accept": "*/*",
        })
        try:
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.read()
        except urllib.error.HTTPError as exc:
            if exc.code in (429, 500, 502, 503, 504):
                retry_after = exc.headers.get("Retry-After")
                delay = BACKOFF_BASE * (2 ** attempt)
                if retry_after and retry_after.isdigit():
                    delay = max(delay, int(retry_after))
                delay += random.uniform(0, 3)
                print(f"    ... HTTP {exc.code}, doi {delay:.0f}s (lan {attempt + 1}/{retries})")
                time.sleep(delay)
                continue
            raise
        except (urllib.error.URLError, TimeoutError) as exc:
            delay = BACKOFF_BASE * (2 ** attempt) * 0.5
            print(f"    ... loi mang ({exc}), doi {delay:.0f}s")
            time.sleep(delay)
    raise RuntimeError(f"Het luot thu: {url[:120]}")


def get_json(url):
    return json.loads(http_get(url).decode("utf-8"))


def dataset_offsets(dataset):
    cache = load_json(OFFSET_CACHE) if os.path.exists(OFFSET_CACHE) else {}
    if dataset in cache:
        return cache[dataset]
    print(f"  Dang lay bang offset cho {dataset}...")
    q = urllib.parse.quote(dataset, safe="")
    stats = get_json(
        f"https://datasets-server.huggingface.co/statistics?dataset={q}&config=default&split=train"
    )
    freq = next(
        s for s in stats["statistics"] if s["column_name"] == "label"
    )["column_statistics"]["frequencies"]
    offsets, running = {}, 0
    for label, count in freq.items():
        offsets[label] = running
        running += count
    cache[dataset] = offsets
    with open(OFFSET_CACHE, "w", encoding="utf-8") as fh:
        json.dump(cache, fh, ensure_ascii=False, indent=2)
    return offsets


def rows(dataset, offset, length=1):
    q = urllib.parse.quote(dataset, safe="")
    data = get_json(
        f"https://datasets-server.huggingface.co/rows?dataset={q}"
        f"&config=default&split=train&offset={offset}&length={length}"
    )
    return data["rows"]


def sharpness(img):
    small = img.convert("L").copy()
    small.thumbnail((320, 320))
    edges = small.filter(ImageFilter.FIND_EDGES)
    return ImageStat.Stat(edges).var[0]


def save_webp(img, path):
    if img.mode != "RGB":
        img = img.convert("RGB")
    img.thumbnail((720, 720), Image.LANCZOS)
    img.save(path, "WEBP", quality=82, method=4)


def download_image(url):
    raw = http_get(url)
    img = Image.open(io.BytesIO(raw))
    img.load()
    return img


def fetch_hf_class(dataset, offset, candidate_count=3, variant=0):
    """Lay vai anh cua lop, chon anh net nhat (hoac variant thu N neu can thay anh)."""
    try:
        batch = rows(dataset, offset, candidate_count)
    except Exception as exc:  # noqa: BLE001
        print("  ! khong lay duoc rows:", exc)
        return None
    scored = []
    for row in batch:
        url = row["row"]["image"]["src"]
        try:
            img = download_image(url)
        except Exception as exc:  # noqa: BLE001
            print("  ! loi tai anh:", str(exc)[:80])
            continue
        score = sharpness(img) * (1.0 + min(img.width * img.height, 4_000_000) / 8_000_000)
        scored.append((score, img))
    if not scored:
        return None
    scored.sort(key=lambda pair: pair[0], reverse=True)
    return scored[min(variant, len(scored) - 1)][1]


CC_BAD = ("non-free", "fair use", "copyright", "gfdl")
TITLE_BAD = ("logo", "map", "diagram", "menu", "packag", "label", "coin", "stamp", "poster", "illustration")


def commons_candidates(query, limit=12):
    params = {
        "action": "query",
        "format": "json",
        "generator": "search",
        "gsrsearch": f"filetype:bitmap {query}",
        "gsrnamespace": "6",
        "gsrlimit": str(limit),
        "prop": "imageinfo",
        "iiprop": "url|size|extmetadata",
        "iiurlwidth": "1000",
    }
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    data = get_json(url)
    return list(data.get("query", {}).get("pages", {}).values())


def fetch_commons(query, keywords):
    """Tim anh Commons, uu tien file co ten chua tu khoa mon an."""
    try:
        pages = commons_candidates(query)
    except Exception as exc:  # noqa: BLE001
        print("  ! loi tim commons:", str(exc)[:100])
        return None, None

    def score(page):
        title = page.get("title", "").lower()
        info = (page.get("imageinfo") or [{}])[0]
        meta = info.get("extmetadata") or {}
        license_name = (meta.get("LicenseShortName", {}).get("value") or "").lower()
        s = 0.0
        if any(k in title for k in keywords):
            s += 10
        if any(bad in title for bad in TITLE_BAD):
            s -= 20
        if any(bad in license_name for bad in CC_BAD):
            return -999
        if info.get("width", 0) >= 1200:
            s += 2
        if info.get("width", 0) < 500 or info.get("height", 0) < 350:
            return -999
        # anh vuong/ngang dep hon anh doc
        w, h = info.get("width", 1), info.get("height", 1)
        s += min(w, h) / max(w, h) * 3
        return s

    ranked = sorted(pages, key=score, reverse=True)
    for page in ranked[:4]:
        info = (page.get("imageinfo") or [{}])[0]
        if not info.get("thumburl"):
            continue
        meta = info.get("extmetadata") or {}
        license_name = (meta.get("LicenseShortName", {}).get("value") or "")
        if any(bad in license_name.lower() for bad in CC_BAD):
            continue
        try:
            img = download_image(wikimedia_thumb(info["thumburl"]))
        except Exception as exc:  # noqa: BLE001
            print("  ! loi tai commons:", str(exc)[:80])
            continue
        artist = (meta.get("Artist", {}).get("value") or "")
        artist = artist.replace("\n", " ")[:140]
        return img, {
            "source": "wikimedia",
            "title": page.get("title"),
            "license": license_name,
            "artist": artist,
            "page": info.get("descriptionurl", ""),
        }
    return None, None


# Tu khoa uu tien khi chon file Commons (tranh anh bao bi / menu / logo).
COMMONS_QUERIES = {
    "burrito": ("chicken burrito", ["burrito"]),
    "cha_gio": ("nem ran cha gio", ["cha gio", "nem ran", "spring roll"]),
    "croissant": ("croissant bakery", ["croissant"]),
    "dimsum": ("dim sum hargow", ["dim sum", "hargow", "har gow", "dumpling"]),
    "fried_chicken": ("fried chicken plate", ["fried chicken"]),
    "kimbap": ("gimbap korean rice roll", ["gimbap", "kimbap"]),
    "milk_tea": ("bubble tea tapioca", ["bubble tea", "boba", "milk tea"]),
    "pho_bo": ("pho bo beef noodle soup", ["pho"]),
    "pho_ga": ("pho ga chicken noodle soup", ["pho"]),
    "tempura": ("tempura shrimp", ["tempura"]),
    "tom_yum": ("tom yum soup bowl", ["tom yum", "tom yam"]),
    "tteokbokki": ("tteokbokki rice cake", ["tteokbokki", "ddeokbokki", "rice cake"]),
    "udon": ("udon noodle bowl", ["udon"]),
    "xoi_ga": ("xoi ga sticky rice chicken", ["xoi", "sticky rice"]),
}

# Anh da tai nhung bi sai mon -> xoa de tai lai.
BAD_IMAGES = ["croissant", "tteokbokki", "tom_yum", "xoi_ga", "pho_bo", "pho_ga", "milk_tea", "kimbap"]

CREDITS_PATH = os.path.join(OUT_DIR, "CREDITS.json")


def save_credits(credits):
    with open(CREDITS_PATH, "w", encoding="utf-8") as fh:
        json.dump(credits, fh, ensure_ascii=False, indent=2)


def load_credits():
    if os.path.exists(CREDITS_PATH):
        try:
            return load_json(CREDITS_PATH)
        except Exception:  # noqa: BLE001
            return []
    return []


def commons_queries_for(food):
    """Danh sach truy van Commons cho mot mon (thu lan luot den khi co anh)."""
    manual = MANUAL_QUERIES.get(food["id"])
    if manual:
        return [manual] if isinstance(manual, str) else list(manual)
    return [commons_query_for(food)]


def commons_query_for(food):
    """Sinh truy van Commons tu ten mon (uon tien tu khoa thu cong neu co)."""
    if food["id"] in MANUAL_COMMONS:
        return MANUAL_COMMONS[food["id"]]
    if food.get("category") == "international":
        return food["id"].replace("_", " ")
    import re

    name = food["name"]
    english = re.search(r"\(([^)]+)\)", name)
    base = re.sub(r"\([^)]*\)", "", name).strip()
    base = base.split(",")[0].strip()
    if english and len(english.group(1)) < 32:
        return f"{base} {english.group(1)}".strip()
    return base


# Truy van da chinh tay cho cac mon anh tu dong bi sai / co watermark.
MANUAL_QUERIES = {
    "banh_bao": ["bánh bao hấp", "bánh bao"],
    "banh_da_lon": ["bánh da lợn", "bánh da heo"],
    "banh_mi": ["bánh mì thịt", "bánh mì Việt Nam"],
    "banh_mi_chao": ["bánh mì chảo", "bánh mì chảo bò"],
    "banh_pia": ["bánh pía", "pia cake"],
    "banh_tai_heo": ["bánh tai heo", "bánh tai heo chiên"],
    "bun_bo_hue": ["bún bò Huế", "bun bo hue"],
    "ca_muoi_xoi": ["cá muối", "cá khô"],
    "ca_ri_ga": ["cà ri gà", "chicken curry"],
    "canh_cua": ["canh cua", "canh cua đồng"],
    "cha_com": ["chả cốm", "chả cốm Hà Nội"],
    "com_chien": ["cơm chiên", "fried rice"],
    "croissant": ["croissant bakery", "croissant bread"],
    "hamburger": ["hamburger beef", "cheeseburger"],
    "kho_muc_nuong": ["mực khô nướng", "grilled dried squid"],
    "lasagna": ["lasagna", "lasagne"],
    "macaroni_and_cheese": ["macaroni and cheese", "mac and cheese"],
    "milk_tea": ["bubble tea", "boba tea"],
    "nachos": ["nachos", "nachos cheese"],
    "oc_buou_hap": ["ốc bươu", "ốc bươu hấp", "apple snail"],
    "oc_len_xao_dua": ["ốc len", "ốc len xào dừa", "mud creeper snail"],
    "pancakes": ["pancakes stack", "buttermilk pancakes"],
    "rau_muong_xao": ["rau muống xào", "water spinach stir fry"],
    "ramen": ["ramen bowl", "ramen noodles"],
    "tau_hu_non": ["tàu hũ non", "silken tofu"],
    "tau_hu_nhoi_thit": ["đậu hũ nhồi thịt", "đậu hũ dồn thịt"],
    "tempura": ["shrimp tempura", "tempura"],
    "thit_dong": ["thịt đông", "thịt đông chân giò"],
    "thit_kho_tau": ["thịt kho tàu", "thịt kho trứng"],
    "tiet_canh": ["tiết canh", "tiết canh vịt"],
    "xoi_ga": ["xôi gà", "chicken sticky rice"],
    "xoi_gac": ["xôi gấc", "gac sticky rice"],
    "xoi_nep_than": ["xôi nếp than", "black sticky rice"],
}
MANUAL_COMMONS = MANUAL_QUERIES


def wikipedia_pageimage(lang, title):
    """Lay anh lead cua mot bai Wikipedia (lang = vi/en)."""
    params = {
        "action": "query",
        "format": "json",
        "prop": "pageimages",
        "piprop": "thumbnail|name",
        "pithumbsize": "1200",
        "redirects": "1",
        "titles": title,
    }
    url = f"https://{lang}.wikipedia.org/w/api.php?" + urllib.parse.urlencode(params)
    data = get_json(url)
    pages = data.get("query", {}).get("pages", {})
    for page in pages.values():
        thumb = (page.get("thumbnail") or {}).get("source")
        if thumb:
            return thumb, page.get("pageimage"), page.get("title")
    return None, None, None


def wikipedia_license(file_name):
    if not file_name:
        return "", ""
    params = {
        "action": "query",
        "format": "json",
        "prop": "imageinfo",
        "iiprop": "extmetadata|url",
        "titles": f"File:{file_name}",
    }
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    try:
        data = get_json(url)
    except Exception:  # noqa: BLE001
        return "", ""
    for page in data.get("query", {}).get("pages", {}).values():
        info = (page.get("imageinfo") or [{}])[0]
        meta = info.get("extmetadata") or {}
        return (
            meta.get("LicenseShortName", {}).get("value", "") or "",
            info.get("descriptionurl", "") or "",
        )
    return "", ""


WIKI_TITLES = {
    "pho_bo": [("vi", "Phở")],
    "pho_ga": [("vi", "Phở gà"), ("vi", "Phở")],
    "thit_dong": [("vi", "Thịt đông")],
    "tiet_canh": [("vi", "Tiết canh")],
    "xoi_nep_than": [("vi", "Xôi")],
    "xoi_ga": [("vi", "Xôi gà"), ("vi", "Xôi")],
    "xoi_gac": [("vi", "Xôi gấc"), ("vi", "Xôi")],
    "oc_buou_hap": [("vi", "Ốc bươu")],
    "oc_len_xao_dua": [("vi", "Ốc len")],
    "banh_mi": [("vi", "Bánh mì Việt Nam"), ("vi", "Bánh mì")],
    "banh_mi_chao": [("vi", "Bánh mì chảo")],
    "banh_pia": [("vi", "Bánh pía")],
    "banh_tai_heo": [("vi", "Bánh tai heo")],
    "banh_da_lon": [("vi", "Bánh da lợn")],
    "canh_cua": [("vi", "Canh cua")],
    "cha_com": [("vi", "Chả cốm")],
    "com_chien": [("vi", "Cơm chiên"), ("en", "Fried rice")],
    "rau_muong_xao": [("vi", "Rau muống")],
    "tau_hu_non": [("vi", "Đậu hũ"), ("en", "Tofu")],
    "tau_hu_nhoi_thit": [("vi", "Đậu hũ nhồi thịt"), ("vi", "Đậu hũ")],
    "thit_kho_tau": [("vi", "Thịt kho tàu")],
    "kho_muc_nuong": [("vi", "Mực khô")],
    "ca_muoi_xoi": [("vi", "Cá muối")],
    "ca_ri_ga": [("vi", "Cà ri")],
    "nem_nuong_nha_trang": [("vi", "Nem nướng")],
    "macaroni_and_cheese": [("en", "Macaroni and cheese")],
    "nachos": [("en", "Nachos")],
    "lasagna": [("en", "Lasagna")],
    "pancakes": [("en", "Pancake")],
    "ramen": [("en", "Ramen")],
    "tom_yum": [("en", "Tom yum")],
    "tempura": [("en", "Tempura")],
    "croissant": [("en", "Croissant")],
    "milk_tea": [("en", "Bubble tea")],
}


def fetch_wikipedia(food_id):
    for lang, title in WIKI_TITLES.get(food_id, []):
        thumb, file_name, real_title = wikipedia_pageimage(lang, title)
        if not thumb:
            continue
        try:
            img = download_image(wikimedia_thumb(thumb))
        except Exception as exc:  # noqa: BLE001
            print("  ! loi tai wikipedia:", str(exc)[:80])
            continue
        license_name, page_url = wikipedia_license(file_name)
        return img, {
            "source": f"wikipedia-{lang}",
            "title": real_title,
            "file": file_name,
            "license": license_name,
            "page": page_url or f"https://{lang}.wikipedia.org/wiki/{urllib.parse.quote(real_title or title)}",
        }
    return None, None


def wikimedia_thumb(url, width=500):
    """Doi URL Wikimedia sang thumbnail 500px tren host thumb (upload chan hotlink).

    Cac kich thuoc hop le: 250, 500, 960, 1280... (xem w.wiki/GHai).
    """
    if not url:
        return url
    url = url.split("?")[0]
    import re

    if "/thumb/" in url:
        url = re.sub(r"/\d+px-", f"/{width}px-", url)
        return url.replace("https://upload.wikimedia.org/", "https://thumb.wikimedia.org/")

    match = re.match(
        r"https://(?:upload|thumb)\.wikimedia\.org/wikipedia/([^/]+)/([0-9a-f])/([0-9a-f]{2})/(.+)$",
        url,
    )
    if not match:
        return url.replace("https://upload.wikimedia.org/", "https://thumb.wikimedia.org/")
    project, first, second, filename = match.groups()
    return (
        f"https://thumb.wikimedia.org/wikipedia/{project}/thumb/"
        f"{first}/{second}/{filename}/{width}px-{filename}"
    )


def fetch_commons_file(file_title):
    """Tai dung mot file cu the tren Commons (kem giay phep + tac gia)."""
    params = {
        "action": "query",
        "format": "json",
        "prop": "imageinfo",
        "iiprop": "url|size|extmetadata",
        "iiurlwidth": "640",
        "titles": file_title,
    }
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    data = get_json(url)
    for page in data.get("query", {}).get("pages", {}).values():
        info = (page.get("imageinfo") or [{}])[0]
        thumb = wikimedia_thumb(info.get("url") or info.get("thumburl"))
        if not thumb:
            continue
        meta = info.get("extmetadata") or {}
        try:
            img = download_image(thumb)
        except Exception as exc:  # noqa: BLE001
            print("  ! loi tai thumb:", str(exc)[:80])
            full = wikimedia_thumb(info.get("url"))
            if not full or full == thumb:
                return None, None
            try:
                img = download_image(full)
            except Exception as exc2:  # noqa: BLE001
                print("  ! loi tai file goc:", str(exc2)[:80])
                return None, None
        return img, {
            "source": "wikimedia",
            "title": page.get("title"),
            "license": meta.get("LicenseShortName", {}).get("value", ""),
            "artist": (meta.get("Artist", {}).get("value", "") or "").replace("\n", " ")[:140],
            "page": info.get("descriptionurl", ""),
        }
    return None, None


# File Commons da chon tay (anh dung mon, giay phep ro rang).
SPECIFIC_FILES = {
    "banh_mi_chao": "File:Một phần bánh mì chảo với trứng và chả (38657217774).jpg",
    "ca_muoi_xoi": "File:Vietnamese fried fish (2656425396).jpg",
    "canh_cua": "File:Canh cua khoai sọ rau rút.jpg",
    "cha_com": "File:Cha Com Ha Noi.jpg",
    "oc_buou_hap": "File:Ốc bươu nhồi thịt tại quán Bà Ti ngày 15 tháng 1 năm 2019 (3).jpg",
    "tiet_canh": "File:Tiết canh vịt.jpg",
    "tau_hu_non": "File:Taiwanese douhua.jpg",
    "tau_hu_nhoi_thit": "File:Hakka-style stuffed tofu at Yunshan Café, Beijing Guangdong Hotel (20240128130539).jpg",
    "banh_mi": "File:Special Baguette (Bánh mì) - Banh Mi Ancient Saigon 2024-12-20.jpg",
    "hot_dog": "File:Hotdog - Evan Swigart.jpg",
    "banh_bao": "File:Baozi Chengdu.JPG",
    "banh_da_lon": "File:Bánh da lơn đậu xanh và lá dứa..jpg",
    "kho_muc_nuong": "File:Close-up view of grilled neritic squid in Taipei after New Year's Eve 2015.jpg",
    "rau_muong_xao": "File:Rau muống xào tỏi.jpg",
    "xoi_nep_than": "File:Black Sticky Rice.jpg",
    "oc_len_xao_dua": "File:Món ốc xào ở quán Ốc La Cà.JPG",
    "nem_nuong_nha_trang": "File:Mẹt nem nướng Nha Trang.jpg",
    "croissant": "File:Chocolate & Almond Croissant - GAIL's Bakery 2025-12-21.jpg",
    "kimbap": "File:Beef kimbap from a DC lunch counter.jpg",
    "milk_tea": "File:Ice bubble milk tea with tapioca balls of Mister Donut in Japan.jpg",
    "pho_bo": "File:Phở bò, Cầu Giấy, Hà Nội.jpg",
    "pho_ga": 'File:Homemade "Phở gà" by A.jpeg',
    "tom_yum": "File:Tom yam kung maenam.jpg",
    "tteokbokki": "File:Korean rice cake (tteokbokki).jpg",
    "xoi_ga": "File:Xôi Gà Trứng Non Lạp Xướng (Sticky rice with chicken and eggs, and chinese sausages).jpg",
}


def main():
    import argparse

    parser = argparse.ArgumentParser(description="Tai anh dai dien cho mon an NutriVision")
    parser.add_argument("--only", default="", help="chi tai cac food_id nay (phay)")
    parser.add_argument("--force", action="store_true", help="tai lai ca file da co (dung voi --only)")
    parser.add_argument("--variant", type=int, default=0, help="chon anh net thu N (0 = net nhat)")
    parser.add_argument("--candidates", type=int, default=3, help="so anh lay ve moi lop")
    parser.add_argument("--commons-fallback", action="store_true",
                        help="tai cac mon con thieu bang Wikimedia Commons (bo qua HuggingFace)")
    parser.add_argument("--wikipedia-fallback", action="store_true",
                        help="tai bang anh lead cua bai Wikipedia (cho cac mon kho tim)")
    parser.add_argument("--specific", action="store_true",
                        help="tai dung file Commons da chon tay trong SPECIFIC_FILES")
    args = parser.parse_args()
    only = {x.strip() for x in args.only.split(",") if x.strip()}

    if only and args.force:
        for food_id in only:
            path = os.path.join(OUT_DIR, f"{food_id}.webp")
            if os.path.exists(path):
                os.remove(path)

    done = {f[:-5] for f in os.listdir(OUT_DIR) if f.endswith(".webp")}

    if args.specific:
        targets = [f for f in FOODS
                   if f["id"] in SPECIFIC_FILES and (not only or f["id"] in only)]
        print(f"Tai file chon tay cho {len(targets)} mon")
        for food in targets:
            food_id = food["id"]
            file_title = SPECIFIC_FILES[food_id]
            print(f"  > {food_id}: {file_title}")
            img, meta = fetch_commons_file(file_title)
            if img is None:
                print(f"  ! khong tai duoc {file_title}")
                continue
            save_webp(img, os.path.join(OUT_DIR, f"{food_id}.webp"))
            credits = [c for c in load_credits() if c.get("food_id") != food_id]
            credits.append({"food_id": food_id, **meta})
            save_credits(credits)
            print(f"  + {food_id} <- {meta.get('license')}")
        done = {f[:-5] for f in os.listdir(OUT_DIR) if f.endswith(".webp")}
        missing = sorted(f["id"] for f in FOODS if f["id"] not in done)
        print(f"\nXong: {len(done)}/{len(FOODS)} mon co anh")
        if missing:
            print("Thieu:", ", ".join(missing))
        return

    if args.wikipedia_fallback:
        targets = [f for f in FOODS
                   if f["id"] in WIKI_TITLES and (not only or f["id"] in only)]
        print(f"Wikipedia fallback cho {len(targets)} mon")
        for food in targets:
            food_id = food["id"]
            print(f"  > {food_id}")
            img, meta = fetch_wikipedia(food_id)
            if img is None:
                print(f"  ! khong co anh wikipedia cho {food_id}")
                continue
            save_webp(img, os.path.join(OUT_DIR, f"{food_id}.webp"))
            credits = [c for c in load_credits() if c.get("food_id") != food_id]
            credits.append({"food_id": food_id, **meta})
            save_credits(credits)
            print(f"  + {food_id} <- {meta.get('title')} ({meta.get('license')})")
        done = {f[:-5] for f in os.listdir(OUT_DIR) if f.endswith(".webp")}
        missing = sorted(f["id"] for f in FOODS if f["id"] not in done)
        print(f"\nXong: {len(done)}/{len(FOODS)} mon co anh")
        if missing:
            print("Thieu:", ", ".join(missing))
        return

    if args.commons_fallback:
        targets = [f for f in FOODS if f["id"] not in done
                   and (not only or f["id"] in only)]
        print(f"Commons fallback cho {len(targets)} mon con thieu")
        for food in targets:
            food_id = food["id"]
            queries = commons_queries_for(food)
            keywords = [w.lower() for w in food_id.replace("_", " ").split() if len(w) > 2]
            img, meta = None, None
            for query in queries:
                keywords += [w.lower() for w in query.split() if len(w) > 2]
                print(f"  > {food_id}: {query}")
                img, meta = fetch_commons(query, keywords)
                if img is not None:
                    break
            if img is None:
                print(f"  ! khong tim thay anh cho {food_id}")
                continue
            save_webp(img, os.path.join(OUT_DIR, f"{food_id}.webp"))
            credits = [c for c in load_credits() if c.get("food_id") != food_id]
            credits.append({"food_id": food_id, **meta})
            save_credits(credits)
            print(f"  + {food_id} <- {meta.get('title')} ({meta.get('license')})")
        done = {f[:-5] for f in os.listdir(OUT_DIR) if f.endswith(".webp")}
        missing = sorted(f["id"] for f in FOODS if f["id"] not in done)
        print(f"\nXong: {len(done)}/{len(FOODS)} mon co anh")
        if missing:
            print("Thieu:", ", ".join(missing))
        return

    vn_off = dataset_offsets(VN_DATASET)
    f101_off = dataset_offsets(F101_DATASET)

    credits = [c for c in load_credits() if c.get("food_id") not in BAD_IMAGES]

    def handle(food_id, dataset, hf_label, source_tag):
        if only and food_id not in only:
            return
        if food_id in done:
            print(f"  = {food_id} (da co)")
            return
        offsets = vn_off if dataset == VN_DATASET else f101_off
        offset = offsets.get(hf_label)
        if offset is None:
            print(f"  ! khong thay lop {hf_label} cho {food_id}")
            return
        print(f"  > {food_id} <- {source_tag}:{hf_label}")
        img = fetch_hf_class(dataset, offset, args.candidates, args.variant)
        if img is None:
            print(f"  ! khong tai duoc {food_id}")
            return
        save_webp(img, os.path.join(OUT_DIR, f"{food_id}.webp"))
        done.add(food_id)
        credits = [c for c in load_credits() if c.get("food_id") != food_id]
        credits.append({
            "food_id": food_id,
            "source": source_tag,
            "hf_label": hf_label,
            "license": "dataset",
        })
        save_credits(credits)
        print(f"  + {food_id} xong")

    # Uu tien cac lop con thieu, sau do moi lop da co (de resume nhanh).
    vn_items = [(lbl, fid) for lbl, fid in VN_MAP.items() if fid in MODEL_FOODS]
    vn_items.sort(key=lambda kv: kv[1] in done)
    for hf_label, food_id in vn_items:
        handle(food_id, VN_DATASET, hf_label, "vn-food-combined-2026")

    f101_items = [(lbl, fid) for lbl, fid in F101_MAP.items() if fid in MODEL_FOODS]
    f101_items.sort(key=lambda kv: kv[1] in done)
    for hf_label, food_id in f101_items:
        handle(food_id, F101_DATASET, hf_label, "food101")

    for food_id, (query, keywords) in COMMONS_QUERIES.items():
        if only and food_id not in only:
            continue
        out_path = os.path.join(OUT_DIR, f"{food_id}.webp")
        if os.path.exists(out_path):
            print(f"  = {food_id} (da co)")
            continue
        print(f"  > commons: {food_id} ({query})")
        img, meta = fetch_commons(query, keywords)
        if img is None:
            print(f"  ! commons khong co anh cho {food_id}")
            continue
        save_webp(img, out_path)
        credits = [c for c in load_credits() if c.get("food_id") != food_id]
        credits.append({"food_id": food_id, **meta})
        save_credits(credits)
        print(f"  + {food_id} <- {meta.get('title')} ({meta.get('license')})")

    done = {f[:-5] for f in os.listdir(OUT_DIR) if f.endswith(".webp")}
    missing = sorted(f["id"] for f in FOODS if f["id"] not in done)
    print(f"\nXong: {len(done)}/{len(FOODS)} mon co anh")
    if missing:
        print("Thieu:", ", ".join(missing))


if __name__ == "__main__":
    main()
