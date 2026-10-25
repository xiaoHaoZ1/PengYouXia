# 任务：朋友侠应用图标（双熊猫侠客）

> 分两步：**第 1 节交给出图模型**生成候选图；用户挑定后经审查方确认，**第 3 节交给实施模型**接入工程。
> 在 `docs/TASK_M0_FIX.md` 完成之后进行。

---

## 0. 侠系列图标规范（从家人侠、守护侠现有图标实测）

参考图（出图时作为风格参考图一起上传）：
- 家人侠：`D:\AIProject\JiaRenXia\apps\android\app\src\main\res\drawable-nodpi\ic_launcher_foreground.png`（侠客抱着一颗心）
- 守护侠：`D:\AIProject\ShouHuXia-dev\guardianapp\src\main\res\drawable-nodpi\ic_launcher_foreground.png`（同一侠客抱着盾牌）

共同风格：
- 主角是一个圆润的小侠客：宽檐**斗笠**、飘起的**披风**，只露出奶白色的脸和两个圆点眼睛。
- 扁平矢量，大块圆润形状，**没有描边、没有渐变、没有纹理、没有阴影**。
- 只用三种颜色：

| 用途 | 颜色 |
|---|---|
| 主体（斗笠、披风、身体） | 深青 `#0C4247` |
| 脸、怀里的物件 | 奶白 `#FBF1DC` |
| 细的点缀线（斗笠边、披风边） | 金黄 `#FBC146` |
| 图标背景（自适应图标背景层） | 明黄 `#F9C147` |

- 前景图 432×432 透明 PNG，人物外框约占画布中间 256×256（家人侠实测外框为 x 88–344、y 96–336），落在自适应图标安全区内。
- 另有一张单色图（monochrome）：同轮廓纯白、透明底，内部细节做成镂空，用于系统主题图标。

## 1. 出图提示词（交给出图模型）

**画面构思**：两只熊猫侠客肩并肩，内侧手臂互相搂着对方肩膀，迈着同样的步子朝前走，像一对结拜兄弟。
沿用侠系列的斗笠和披风，熊猫的黑色部分改用系列深青色。

### 1.1 英文提示词（Midjourney / DALL·E / GPT-Image / Flux 等）

```
Flat vector app icon mascot, in exactly the same style as the attached reference icons
(a small round wandering-swordsman hero in a wide conical bamboo hat and a flowing cloak,
smooth rounded shapes, no outlines, strict three-color palette).

Subject: two chubby panda heroes standing shoulder to shoulder, walking forward toward the viewer
in step, each with its inner arm wrapped around the other's shoulders, like two sworn brothers.
Both wear the same wide conical bamboo hat as the reference hero, tilted slightly outward,
with a thin gold rim line, and short cloaks with thin gold edge lines.
Panda faces: cream face, dark teardrop eye patches, small round dark dot eyes,
calm friendly expression, round ears just visible under the hat brims.
Bodies overlap a little in the middle so the pair reads as one compact rounded silhouette.

Colors only: dark teal #0C4247 for panda dark fur, hats and cloaks; cream #FBF1DC for white fur,
faces and bellies; gold #FBC146 for thin accent lines only. Plain solid background #F9C147.

Composition: centered, nearly symmetrical, the whole pair fits inside a centered circle
about 60% of the canvas width, generous empty margin. Bold simple shapes that stay readable at 48 px.
Square 1:1, 1024x1024.
```

负面提示词（支持时填写）：

```
photorealistic, 3D render, gradient, glossy, outline stroke, text, letters, watermark,
scenery, ground, cast shadow, more than two characters, weapons, swords, bamboo,
red, pink, black, extra colors, tiny details, cropped edges
```

### 1.2 中文提示词（通义万相 / 即梦 / 豆包 / 文心一格等）

```
扁平矢量风格的手机应用图标吉祥物，风格与上传的两张参考图完全一致：圆润的小侠客，戴宽檐斗笠、披飘逸披风，
大块圆润形状，无描边，严格三色。

画面：两只胖乎乎的熊猫侠客肩并肩，内侧的手臂互相搂着对方的肩膀，迈着同样的步子朝镜头走来，像一对结拜兄弟。
两只熊猫都戴着和参考图一样的宽檐斗笠，斗笠微微向外倾斜，帽檐有一条细金线；披着短披风，披风边有细金线。
熊猫的脸是奶白色，深色水滴形眼圈，小圆点眼睛，神情平和友善，圆耳朵从帽檐下露出一点。
两个身体在中间略有重叠，整体是一个紧凑圆润的轮廓。

只用这几种颜色：深青色 #0C4247（熊猫黑毛、斗笠、披风），奶白色 #FBF1DC（熊猫白毛、脸、肚子），
金黄色 #FBC146（只用于细点缀线），纯色背景 #F9C147。

构图：居中，接近左右对称，两只熊猫整体放在画面中央约 60% 宽的圆形范围内，四周大量留白，
缩小到 48 像素仍然清晰可辨。正方形 1:1，1024×1024。

不要：写实、3D、渐变、高光、描边、文字、水印、背景场景、地面、投影、第三个角色、武器、竹子、红色、粉色、纯黑色、琐碎细节、画面被裁切。
```

### 1.3 备选构图（第一轮效果不好时再试）

- **B：3/4 侧面同向前行**：两只熊猫朝画面右前方迈步，前后略错开，外侧披风向后飘，动感更强，脸仍可见。
- **C：只留一顶大斗笠**：两只熊猫共撑在一顶加宽的斗笠下（像共用一把伞），轮廓更简洁，小尺寸更易读。
- **D：背影**：两兄弟搂肩背对镜头向远处走，斗笠与披风是主体。氛围最好，但看不到熊猫脸，辨识度最低，仅作参考。

每种构图至少出 4 张候选。

### 1.4 挑选标准（用户先挑，审查方复核）

1. 缩到 48×48 像素看：能认出是**两只熊猫**、**戴斗笠**、**搂着肩**。
2. 放在家人侠、守护侠图标旁边，一眼能看出是同一系列。
3. 只有三种颜色加背景色，没有渐变、描边、杂色。
4. 两只熊猫大小一致、地位平等（兄弟，不是一大一小）。
5. 表情友善，不凶、不卖萌过度。

## 2. 出图后的整理（出图模型或用户完成均可）

选定的图交给审查方确认后，整理成以下文件（放在 `assets/brand/`，入库）：

| 文件 | 规格 |
|---|---|
| `icon_master.png` | 选定原图，1024×1024，带 `#F9C147` 背景 |
| `ic_launcher_foreground.png` | 432×432 透明底，只有人物；人物外框约 256×256 居中（与家人侠一致） |
| `ic_launcher_monochrome.png` | 432×432 透明底，同轮廓纯白 `#FFFFFF`，眼睛、眼圈、金线等内部细节做成镂空 |
| `logo.png` | 512×512，带 `#F9C147` 背景、圆角正方形，用于以后的官网 / 分享 |

出图模型通常给不出精确的色值和透明底：需要抠图、把颜色统一校正到第 0 节的三色、去掉杂色和半透明毛边。

## 3. 接入工程（交给实施模型）

1. 把 `assets/brand/ic_launcher_foreground.png`、`ic_launcher_monochrome.png` 复制到 `app/src/main/res/drawable-nodpi/`
   （与家人侠、守护侠同一做法），删除现有矢量图 `app/src/main/res/drawable/ic_launcher_foreground.xml`。
2. `mipmap-anydpi/ic_launcher.xml`、`ic_launcher_round.xml`：前景、单色层指向上面两张图；背景层用颜色资源 `ic_launcher_background = #F9C147`
   （放 `values/colors.xml`，深色模式不另设）。
3. 组件样例页（测试源码 `ComponentGallery.kt`）的"应用图标"一节改为展示新图标：圆形、圆角方形两种遮罩各一个，外加 48dp 小尺寸一个。
4. 跑完整验证任务全部通过（lint 0 error），重新生成 `m0_components_*.png`；一次提交，不推送。
5. 汇报：提交哈希、截图路径、真机清单（装 debug 包 → 看桌面图标；系统开启"主题图标"后再看单色图标）。
