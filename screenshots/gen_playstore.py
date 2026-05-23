"""
AppTest Play Store screenshot compositor
Wraps real device screenshots (720x1600) in a branded 1080x1920 card.

Layout per card:
  - 1080x1920 canvas, dark gradient brand background
  - Device screenshot scaled+centered (828x1840 → fills most of canvas)
  - Caption strip at bottom (80px)
  - Status bar area slightly dimmed for cleanliness

Output files: play_01_*.png … play_0N_*.png (in same directory as this script)
"""

from PIL import Image, ImageDraw, ImageFont
import os, math

HERE = os.path.dirname(os.path.abspath(__file__))

# ── Brand palette (matches gen_assets.py) ────────────────────────────────────
C_BG     = (242, 244, 251)    # app background
C_DARK   = (45,  52,  80)
C_ACCENT = (91, 139, 245)
C_ACCENT2= (120,165, 255)
C_WHITE  = (255,255,255)
C_SURFACE= (230,235, 250)

FONT_EN_BOLD = "C:/Windows/Fonts/arialbd.ttf"
FONT_ZH_BOLD = "C:/Windows/Fonts/msjhbd.ttc"
FONT_ZH_REG  = "C:/Windows/Fonts/msjh.ttc"

def load_font(path, size):
    try:    return ImageFont.truetype(path, size)
    except: return ImageFont.load_default()


# ── Canvas constants ──────────────────────────────────────────────────────────
CW, CH = 1080, 1920            # canvas (standard Play Store phone)
SCREEN_W, SCREEN_H = 828, 1840  # device screen area inside canvas
SCREEN_X = (CW - SCREEN_W) // 2   # 126
SCREEN_Y = 10                       # 10 px top gap
CAPTION_H = 70                      # bottom caption strip height


def make_bg(draw, w, h):
    """Dark gradient background."""
    for y in range(h):
        t = y / h
        r = int(C_DARK[0] + (28  - C_DARK[0]) * t)
        g = int(C_DARK[1] + (36  - C_DARK[1]) * t)
        b = int(C_DARK[2] + (78  - C_DARK[2]) * t)
        draw.line([(0, y), (w, y)], fill=(r, g, b))


def make_frame(src_path: str, caption_zh: str, caption_en: str, out_name: str):
    src = Image.open(os.path.join(HERE, src_path)).convert("RGB")

    # Scale screenshot to fit SCREEN_W x SCREEN_H exactly
    scale = min(SCREEN_W / src.width, SCREEN_H / src.height)
    new_w = int(src.width  * scale)
    new_h = int(src.height * scale)
    src_scaled = src.resize((new_w, new_h), Image.LANCZOS)

    # Compose canvas
    canvas = Image.new("RGB", (CW, CH), C_DARK)
    draw   = ImageDraw.Draw(canvas)
    make_bg(draw, CW, CH)

    # Phone frame shadow
    sx, sy = SCREEN_X - 2, SCREEN_Y - 2
    ex, ey = SCREEN_X + new_w + 2, SCREEN_Y + new_h + 2
    for shadow_r in [28, 24, 20]:
        draw.rounded_rectangle(
            [sx-8, sy-8, ex+8, ey+8],
            radius=shadow_r, fill=(0,0,0)
        )

    # Paste screenshot
    paste_x = SCREEN_X + (SCREEN_W - new_w) // 2
    paste_y = SCREEN_Y + (SCREEN_H - new_h) // 2
    canvas.paste(src_scaled, (paste_x, paste_y))

    # Phone outer frame
    draw.rounded_rectangle(
        [paste_x-3, paste_y-3, paste_x+new_w+3, paste_y+new_h+3],
        radius=28, outline=(80,90,130), width=3
    )

    # Caption strip at very bottom
    cap_y = CH - CAPTION_H
    draw.rectangle([0, cap_y, CW, CH], fill=C_DARK)
    # thin top line
    draw.line([(40, cap_y+2), (CW-40, cap_y+2)], fill=C_ACCENT, width=2)

    fz = load_font(FONT_ZH_BOLD, 32)
    fe = load_font(FONT_EN_BOLD, 22)
    # zh caption centred
    draw.text((CW//2, cap_y + 20), caption_zh, font=fz, fill=C_WHITE, anchor="mt")
    # en subtitle
    draw.text((CW//2, cap_y + 48), caption_en, font=fe, fill=C_ACCENT2, anchor="mt")

    out_path = os.path.join(HERE, out_name)
    canvas.save(out_path, "PNG")
    w, h = canvas.size
    print(f"  OK {out_name}  ({w}x{h})")
    return out_path


# ── Screenshot list ───────────────────────────────────────────────────────────
SCREENS = [
    # (src_file, zh_caption, en_caption, output_name)
    (
        "fresh_launch.png",
        "加入 1,238 位開發者的互測網路",
        "Sign in with Google or Email — it's free",
        "play_01_signin.png",
    ),
    (
        "01_home.png",
        "今日配對，一鍵加入",
        "Daily AI-matched apps — tap to review",
        "play_02_home.png",
    ),
    (
        "05_appdetail.png",
        "了解每一個配對的理由",
        "See exactly why you were matched",
        "play_03_appdetail.png",
    ),
    (
        "03_testing.png",
        "一眼掌握所有進行中測試",
        "Track every active test at a glance",
        "play_04_testing.png",
    ),
    (
        "04_profile.png",
        "你的信用，每次測試都在累積",
        "Reputation earned test by test — Silver tier",
        "play_05_profile.png",
    ),
    (
        "02_myapps.png",
        "你的 App 招募進度一覽",
        "Your apps — watch tester slots fill up",
        "play_06_myapps.png",
    ),
]


if __name__ == "__main__":
    print("Generating Play Store screenshots …")
    for args in SCREENS:
        make_frame(*args)
    print("\nDone — 6 screenshots ready for Play Console upload.")
    print("Path:", HERE)
