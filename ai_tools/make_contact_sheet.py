"""Tao contact sheet (luoi anh + ten mon) de review anh da tai.

Dung: py -3.14 ai_tools/make_contact_sheet.py
Ket qua: ai_tools/_preview/sheet_1.png, sheet_2.png, ...
"""
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

sys.stdout.reconfigure(encoding="utf-8")

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app", "src", "main", "assets")
IMG_DIR = os.path.join(ASSETS, "food_images")
OUT_DIR = os.path.join(ROOT, "ai_tools", "_preview")
os.makedirs(OUT_DIR, exist_ok=True)

COLS, ROWS = 8, 5
CELL_W, CELL_H = 200, 232
IMG_SIZE = 176

FONTS = [
    r"C:\Windows\Fonts\arial.ttf",
    r"C:\Windows\Fonts\segoeui.ttf",
]


def load_font(size):
    for path in FONTS:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


foods = json.load(open(os.path.join(ASSETS, "foods_nutrition.json"), encoding="utf-8"))
font = load_font(15)
font_small = load_font(13)
title_font = load_font(28)

per_page = COLS * ROWS
pages = [foods[i:i + per_page] for i in range(0, len(foods), per_page)]

for page_index, page_foods in enumerate(pages, start=1):
    sheet = Image.new("RGB", (COLS * CELL_W, ROWS * CELL_H + 60), "white")
    draw = ImageDraw.Draw(sheet)
    draw.text((14, 14), f"NutriVision - anh mon an (trang {page_index}/{len(pages)})",
              fill="black", font=title_font)

    for index, food in enumerate(page_foods):
        col, row = index % COLS, index // COLS
        x = col * CELL_W
        y = row * CELL_H + 60
        path = os.path.join(IMG_DIR, f"{food['id']}.webp")
        if os.path.exists(path):
            img = Image.open(path).convert("RGB")
            img.thumbnail((IMG_SIZE, IMG_SIZE))
            ox = x + (CELL_W - img.width) // 2
            oy = y + (IMG_SIZE - img.height) // 2
            sheet.paste(img, (ox, oy))
        else:
            draw.rectangle([x + 8, y + 4, x + CELL_W - 8, y + IMG_SIZE], outline="#cc0000", width=3)
            draw.text((x + 20, y + IMG_SIZE // 2 - 10), "THIEU ANH", fill="#cc0000", font=font)
        name = food["name"]
        if len(name) > 30:
            name = name[:29] + "..."
        draw.text((x + 8, y + IMG_SIZE + 6), name, fill="black", font=font_small)
        draw.text((x + 8, y + IMG_SIZE + 24), food["id"], fill="#666666", font=font_small)

    out_path = os.path.join(OUT_DIR, f"sheet_{page_index}.png")
    sheet.save(out_path, "PNG")
    print(f"+ {out_path} ({len(page_foods)} mon)")
