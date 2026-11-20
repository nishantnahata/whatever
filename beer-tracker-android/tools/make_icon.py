"""Draws the launcher icon (a pint of beer on an amber-cream tile) at every density."""
import os, sys
from PIL import Image, ImageDraw

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
S = 768  # draw large, downscale for smooth edges

BG = (42, 31, 20, 255)

def draw(tile=True):
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if tile:
        d.rounded_rectangle([24, 24, S - 24, S - 24], radius=170, fill=BG)
    # glass outline points (trapezoid)
    top_y, bot_y = 170, 640
    tl, tr, bl, br = 230, 538, 270, 498
    def x_at(y, left):
        t = (y - top_y) / (bot_y - top_y)
        return (tl + (bl - tl) * t) if left else (tr + (br - tr) * t)
    beer_y, foam_y = 290, 215
    d.polygon([(x_at(beer_y, 1), beer_y), (x_at(beer_y, 0), beer_y), (br, bot_y), (bl, bot_y)], fill=(224, 154, 31, 255))
    d.polygon([(x_at(foam_y, 1), foam_y), (x_at(foam_y, 0), foam_y), (x_at(beer_y, 0), beer_y), (x_at(beer_y, 1), beer_y)], fill=(255, 247, 230, 255))
    for cx, cy, r in [(300, 210, 48), (370, 190, 58), (450, 200, 52), (510, 220, 36)]:
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(255, 247, 230, 255))
    # bubbles
    for cx, cy, r in [(330, 420, 12), (420, 360, 9), (460, 500, 11), (360, 560, 8)]:
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(246, 196, 92, 255))
    d.line([(tl, top_y), (bl, bot_y), (br, bot_y), (tr, top_y)], fill=(245, 234, 216, 255), width=18, joint="curve")
    # handle
    d.arc([430, 300, 620, 520], start=-90, end=90, fill=(245, 234, 216, 255), width=22)
    return img

def glass_on(canvas_px, glass_frac, bg=None):
    """The glass alone, centred on a square canvas, its height = glass_frac of the canvas."""
    g = draw(tile=False)
    g = g.crop(g.getbbox())
    h = int(canvas_px * glass_frac)
    w = round(g.width * h / g.height)
    g = g.resize((w, h), Image.LANCZOS)
    c = Image.new("RGBA", (canvas_px, canvas_px), bg or (0, 0, 0, 0))
    # nudge left a little so the glass body (not glass + handle) sits in the centre
    c.alpha_composite(g, ((canvas_px - w) // 2 - canvas_px // 40, (canvas_px - h) // 2))
    return c

# Usage: make_icon.py <res dir> <store dir>
res, store = sys.argv[1], sys.argv[2]
big = draw()
FG = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
for name, px in SIZES.items():
    p = os.path.join(res, f"mipmap-{name}")
    os.makedirs(p, exist_ok=True)
    big.resize((px, px), Image.LANCZOS).save(os.path.join(p, "ic_launcher.png"))
    # Adaptive icon foreground (Android 8+): 108dp canvas, art kept inside the 66dp safe zone.
    glass_on(FG[name], 0.50).save(os.path.join(p, "ic_launcher_foreground.png"))
os.makedirs(store, exist_ok=True)
# Play Store icon: full-bleed square (Play rounds the corners itself), 512x512.
glass_on(512, 0.66, BG).convert("RGB").save(os.path.join(store, "icon-512.png"))
