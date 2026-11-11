"""Tim file tren Wikimedia Commons va in ra ten + giay phep + kich thuoc."""
import json
import sys
import time
import urllib.parse
import urllib.request

sys.stdout.reconfigure(encoding="utf-8")
UA = "NutriVisionStudentProject/1.0 (educational research; contact: student@example.com)"


def search(query, limit=8):
    params = {
        "action": "query",
        "format": "json",
        "generator": "search",
        "gsrsearch": f"filetype:bitmap {query}",
        "gsrnamespace": "6",
        "gsrlimit": str(limit),
        "prop": "imageinfo",
        "iiprop": "url|size|extmetadata",
    }
    url = "https://commons.wikimedia.org/w/api.php?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as resp:
        data = json.loads(resp.read().decode("utf-8"))
    pages = data.get("query", {}).get("pages", {})
    rows = []
    for page in pages.values():
        info = (page.get("imageinfo") or [{}])[0]
        meta = info.get("extmetadata") or {}
        rows.append({
            "title": page.get("title"),
            "w": info.get("width"),
            "h": info.get("height"),
            "license": meta.get("LicenseShortName", {}).get("value", ""),
            "url": info.get("url"),
        })
    return rows


def main():
    for query in sys.argv[1:]:
        print(f"\n### {query}")
        try:
            rows = search(query)
        except Exception as exc:  # noqa: BLE001
            print("  loi:", exc)
            continue
        for row in rows:
            print(f"  {row['license']:<16} {row['w']}x{row['h']}  {row['title']}")
        time.sleep(2)


if __name__ == "__main__":
    main()
