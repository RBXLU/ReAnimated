# -*- coding: utf-8 -*-
"""Баннеры для описания мода на Modrinth/CurseForge и для README.

Как пользоваться:

    pip install Pillow
    curl -sSLo docs/PressStart2P-Regular.ttf \
      https://raw.githubusercontent.com/google/fonts/main/ofl/pressstart2p/PressStart2P-Regular.ttf
    python3 docs/make_banners.py

Картинки складываются в docs/images/ и подключаются в описании по прямым ссылкам
на raw.githubusercontent.com — Modrinth и CurseForge показывают только абсолютные
адреса.

Шрифт — Press Start 2P (SIL Open Font License 1.1), он не входит в репозиторий:
скачайте его командой выше или укажите свой пиксельный шрифт в FONT.

Приём, за счёт которого получается «майнкрафтовая» чёткость: текст рисуется в
маленьком размере и увеличивается методом NEAREST — края остаются пиксельными,
без замыливания, как у баннеров других модов.
"""

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
FONT = os.path.join(HERE, 'PressStart2P-Regular.ttf')
OUT = os.path.join(HERE, 'images')
os.makedirs(OUT, exist_ok=True)

BG        = (14, 14, 22, 255)
PANEL     = (23, 23, 38, 255)
BORDER    = (52, 52, 82, 255)
VIOLET    = (139, 108, 255)
CYAN      = (55, 216, 242)
AMBER     = (255, 194, 75)
TEXT      = (242, 242, 247)
MUTED     = (154, 154, 176)

def font(px):
    return ImageFont.truetype(FONT, px)

def text_mask(txt, px):
    """Маска текста в малом масштабе — её потом увеличиваем без сглаживания."""
    f = font(px)
    tmp = Image.new('L', (10, 10))
    box = ImageDraw.Draw(tmp).textbbox((0, 0), txt, font=f)
    w, h = box[2] - box[0] + 4, box[3] - box[1] + 4
    m = Image.new('L', (w, h), 0)
    ImageDraw.Draw(m).text((2 - box[0], 2 - box[1]), txt, font=f, fill=255)
    return m

def gradient(size, c1, c2, horizontal=True):
    w, h = size
    g = Image.new('RGB', (w, h))
    d = ImageDraw.Draw(g)
    n = w if horizontal else h
    for i in range(n):
        t = i / max(1, n - 1)
        c = tuple(int(c1[k] + (c2[k] - c1[k]) * t) for k in range(3))
        if horizontal:
            d.line([(i, 0), (i, h)], fill=c)
        else:
            d.line([(0, i), (w, i)], fill=c)
    return g

def pixel_text(txt, px, scale, fill=None, grad=None, shadow=(0, 0, 0, 190),
               shadow_offset=1, outline=None, outline_width=1):
    """Пиксельная надпись: тень, обводка и заливка цветом или градиентом."""
    m = text_mask(txt, px)
    w, h = m.size
    pad = outline_width + abs(shadow_offset) + 1
    canvas = Image.new('RGBA', (w + pad * 2, h + pad * 2), (0, 0, 0, 0))

    if outline:
        o = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
        col = Image.new('RGBA', canvas.size, outline)
        for dx in range(-outline_width, outline_width + 1):
            for dy in range(-outline_width, outline_width + 1):
                if dx or dy:
                    o.paste(col, (0, 0), Image.new('L', canvas.size, 0).__class__.new('L', canvas.size, 0))
        # обводка проще: несколько сдвинутых копий маски
        o = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
        for dx in range(-outline_width, outline_width + 1):
            for dy in range(-outline_width, outline_width + 1):
                if dx or dy:
                    layer = Image.new('RGBA', canvas.size, outline)
                    o.paste(layer, (0, 0), _shift(m, canvas.size, pad + dx, pad + dy))
        canvas.alpha_composite(o)

    if shadow:
        sh = Image.new('RGBA', canvas.size, shadow)
        canvas.paste(sh, (0, 0), _shift(m, canvas.size, pad + shadow_offset, pad + shadow_offset))

    body = Image.new('RGBA', canvas.size, (0, 0, 0, 0))
    if grad:
        g = gradient(canvas.size, grad[0], grad[1]).convert('RGBA')
        body.paste(g, (0, 0), _shift(m, canvas.size, pad, pad))
    else:
        body.paste(Image.new('RGBA', canvas.size, fill + (255,)), (0, 0), _shift(m, canvas.size, pad, pad))
    canvas.alpha_composite(body)

    return canvas.resize((canvas.width * scale, canvas.height * scale), Image.NEAREST)

def _shift(mask, size, x, y):
    out = Image.new('L', size, 0)
    out.paste(mask, (x, y))
    return out

def plate(size, radius=10, fill=PANEL, border=BORDER, border_width=2):
    img = Image.new('RGBA', size, (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius=radius, fill=fill,
                        outline=border, width=border_width)
    return img

def grid(img, step=16, color=(255, 255, 255, 5)):
    d = ImageDraw.Draw(img, 'RGBA')
    for x in range(0, img.width, step):
        d.line([(x, 0), (x, img.height)], fill=color)
    for y in range(0, img.height, step):
        d.line([(0, y), (img.width, y)], fill=color)
    return img

def shade(img, box, blur=70, alpha=210):
    """Мягкое затемнение: под надписью сетка не должна лезть в глаза."""
    layer = Image.new('RGBA', img.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(box, fill=(8, 8, 14, alpha))
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(blur)))
    return img

def glow(img, box, color, blur=28, alpha=110):
    layer = Image.new('RGBA', img.size, (0, 0, 0, 0))
    ImageDraw.Draw(layer).ellipse(box, fill=color + (alpha,))
    img.alpha_composite(layer.filter(ImageFilter.GaussianBlur(blur)))
    return img

# --------------------------------------------------------------------- шапка

def hero():
    W, H = 1280, 380
    img = Image.new('RGBA', (W, H), BG)
    grid(img, 24, (255, 255, 255, 6))
    # Свечение приглушённое и далеко от центра: оно должно намекать на цвет, а не
    # заливать половину картинки.
    glow(img, (-260, 150, 300, 480), VIOLET, blur=60, alpha=60)
    glow(img, (980, -80, 1540, 250), CYAN, blur=60, alpha=55)
    shade(img, (140, 40, 1140, 340))

    # Шлейф движения: копии названия левее и бледнее — мод же про анимацию.
    title = pixel_text('REANIMATED', 16, 5, grad=(VIOLET, CYAN), shadow=(0, 0, 0, 200), shadow_offset=1)
    tx = (W - title.width) // 2
    ty = 96
    for dx, a in ((-190, 14), (-130, 22), (-66, 34)):
        ghost = title.copy()
        ghost.putalpha(ghost.getchannel('A').point(lambda v, a=a: v * a // 255))
        img.alpha_composite(ghost, (tx + dx, ty))
    del dx, a
    img.alpha_composite(title, (tx, ty))

    sub = pixel_text('SMOOTH ANIMATIONS FOR THE ENTIRE UI', 8, 2, fill=MUTED, shadow=None)
    img.alpha_composite(sub, ((W - sub.width) // 2, ty + title.height + 26))

    chip_text = pixel_text('V 1.7', 8, 2, fill=(18, 18, 28), shadow=None)
    cw, ch = chip_text.width + 34, chip_text.height + 22
    chip = Image.new('RGBA', (cw, ch), (0, 0, 0, 0))
    ImageDraw.Draw(chip).rounded_rectangle([0, 0, cw - 1, ch - 1], radius=8, fill=AMBER)
    chip.alpha_composite(chip_text, (17, 11))
    img.alpha_composite(chip, ((W - cw) // 2, ty + title.height + 74))

    bar = gradient((W, 6), VIOLET, CYAN).convert('RGBA')
    img.alpha_composite(bar, (0, H - 6))
    img.convert('RGB').save(os.path.join(OUT, 'hero.png'))

# ---------------------------------------------------------------- разделитель

def divider():
    W, H = 1280, 26
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    g = gradient((W, H), VIOLET, CYAN).convert('RGBA')
    mask = Image.new('L', (W, H), 0)
    md = ImageDraw.Draw(mask)
    step = 32
    for x in range(0, W + step, step):
        md.polygon([(x, 6), (x + 14, 6), (x + 8, H - 6), (x - 6, H - 6)], fill=210)
    img.paste(g, (0, 0), mask)
    img.save(os.path.join(OUT, 'divider.png'))

# ------------------------------------------------------------ заголовки секций

def header(name, label, icon_color):
    W, H = 1280, 104
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    img.alpha_composite(plate((W, H), radius=14, fill=(20, 20, 31, 255), border=(42, 42, 68, 255)))
    # Лёгкое свечение в цвет секции — вместо сетки, которая на светлой плашке
    # превращалась в миллиметровку.
    glow(img, (-140, -60, 380, 200), icon_color, blur=55, alpha=45)

    tab = Image.new('RGBA', (12, H - 30), (0, 0, 0, 0))
    ImageDraw.Draw(tab).rounded_rectangle([0, 0, 11, H - 31], radius=5, fill=icon_color + (255,))
    img.alpha_composite(tab, (24, 15))

    txt = pixel_text(label, 12, 3, grad=(icon_color, TEXT), shadow=(0, 0, 0, 210))
    img.alpha_composite(txt, (58, (H - txt.height) // 2))

    dots = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    dd = ImageDraw.Draw(dots)
    for i in range(16):
        x = W - 44 - i * 18
        a = int(130 * (1 - i / 16))
        dd.rectangle([x, H // 2 - 3, x + 6, H // 2 + 3], fill=icon_color + (a,))
    img.alpha_composite(dots)
    img.save(os.path.join(OUT, 'header_%s.png' % name))

# ------------------------------------------------------- схема «как это едет»

def motion():
    """Три стадии выезда экрана — то, чего не покажешь скриншотом статичной игры."""
    W, H = 1280, 540
    img = Image.new('RGBA', (W, H), BG)
    grid(img, 26, (255, 255, 255, 4))
    glow(img, (-200, 200, 260, 560), VIOLET, blur=70, alpha=45)
    glow(img, (1020, -80, 1520, 260), CYAN, blur=70, alpha=45)
    shade(img, (90, 20, 1190, 500), blur=80, alpha=200)

    cap = pixel_text('HOW A SCREEN ARRIVES', 10, 3, grad=(CYAN, TEXT), shadow=(0, 0, 0, 210))
    img.alpha_composite(cap, ((W - cap.width) // 2, 34))

    pw, ph = 300, 200
    base_x, base_y = 130, 140
    stages = ((0, 80, 90), (150, 38, 165), (300, 0, 255))
    for i, (ms, dy, alpha) in enumerate(stages):
        x = base_x + i * (pw + 60)
        accent = VIOLET + (255,) if i == 2 else (60, 60, 96, 255)
        p = plate((pw, ph), radius=14, fill=(26, 26, 44, 255), border=accent)
        d = ImageDraw.Draw(p)
        for r in range(4):
            y = 32 + r * 38
            d.rounded_rectangle([26, y, pw - 26, y + 26], radius=7,
                                fill=(46, 46, 74, 255) if r else VIOLET + (255,))
        p.putalpha(p.getchannel('A').point(lambda v, a=alpha: v * a // 255))
        img.alpha_composite(p, (x, base_y + dy))

        lab = pixel_text('%d MS' % ms, 8, 2, fill=AMBER if i == 2 else MUTED, shadow=(0, 0, 0, 180))
        # Подписи на одной высоте, а не под каждой панелью: панели стоят на разной.
        img.alpha_composite(lab, (x + (pw - lab.width) // 2, 436))

    arrow = ImageDraw.Draw(img, 'RGBA')
    for i in range(2):
        x = base_x + pw + 14 + i * (pw + 60)
        y = base_y + ph // 2 + 20
        arrow.polygon([(x, y - 8), (x + 28, y - 8), (x + 28, y - 18), (x + 50, y),
                       (x + 28, y + 18), (x + 28, y + 8), (x, y + 8)], fill=CYAN + (210,))

    note = pixel_text('SLIDE  /  FADE  /  SETTLE', 8, 2, fill=MUTED, shadow=None)
    img.alpha_composite(note, ((W - note.width) // 2, 480))

    bar = gradient((W, 6), VIOLET, CYAN).convert('RGBA')
    img.alpha_composite(bar, (0, H - 6))
    img.convert('RGB').save(os.path.join(OUT, 'motion.png'))

if __name__ == '__main__':
    hero()
    divider()
    motion()
    for name, label, color in (
        ('about', 'ABOUT', CYAN),
        ('features', 'FEATURES', VIOLET),
        ('presets', 'PRESETS', AMBER),
        ('settings', 'SETTINGS', CYAN),
        ('api', 'EXTENSION API', VIOLET),
        ('install', 'INSTALL', AMBER),
        ('versions', 'VERSIONS', CYAN),
        ('credits', 'CREDITS', VIOLET),
    ):
        header(name, label, color)
    print('\n'.join(sorted(os.listdir(OUT))))
