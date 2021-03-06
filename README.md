<!--suppress HtmlDeprecatedAttribute -->
<h1 align="center">BiliDownload</h1>

<p align="center">
    <!--suppress CheckImageSize -->
<img src="bin/logo-super-ellipse.svg" width=309 height=309 alt="">
</p>

<h3 align="center">本项目通过调用B站 WEB 端与 TV 端的 API 实现了对B站视频的下载。</h3>
<p align="center">
    <a href="https://github.com/Naptie/BiliDownload/issues" style="text-decoration:none">
        <img src="https://img.shields.io/github/issues/Naptie/BiliDownload.svg" alt="GitHub issues"/>
    </a>
    <a href="https://github.com/Naptie/BiliDownload/stargazers" style="text-decoration:none" >
        <img src="https://img.shields.io/github/stars/Naptie/BiliDownload.svg" alt="GitHub stars"/>
    </a>
    <a href="https://github.com/Naptie/BiliDownload/network" style="text-decoration:none" >
        <img src="https://img.shields.io/github/forks/Naptie/BiliDownload.svg" alt="GitHub forks"/>
    </a>
    <a href="https://github.com/Naptie/BiliDownload/blob/master/LICENSE" style="text-decoration:none" >
        <img src="https://img.shields.io/github/license/Naptie/BiliDownload.svg" alt="GitHub license"/>
    </a>
</p>

---

# 声明

1. 本项目遵循 MIT 协议，因滥用本项目提供的服务而造成的后果与本人无关；

2. 由于本项目较为特殊，不排除随时删档的可能性。

---

# 使用前

1. 请确保您已安装 [JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) 或更高版本。

2. 如需使用音视频合并功能，请确保您已安装 [FFmpeg](https://www.ffmpeg.org)。

---

# 使用流程

1. **[可跳过]** 使用 `mvn clean package` 命令进行编译。

2. 输入 `java -jar bili-download-1.3.6-jar-with-dependencies.jar`，进入程序。若您希望启用 debug 模式，请在参数中输入 `debug`
   ，即输入 `java -jar bili-download-1.3.6-jar-with-dependencies.jar debug`，这将显示一些调试信息。若仅需从 URL
   下载文件，请在参数中输入 `direct "<url>" "<path>"`
   ，如输入：
   ```
   java -jar bili-download-1.3.6-jar-with-dependencies.jar direct "http://upos-sz-mirrorkodo.bilivideo.com/upgcxcode/90/37/315703790/315703790-1-30336.m4s?e=ig8euxZM2rNcNbdlhoNvNC8BqJIzNbfqXBvEuENvNC8aNEVEtEvE9IMvXBvE2ENvNCImNEVEIj0Y2J_aug859r1qXg8gNEVE5XREto8z5JZC2X2gkX5L5F1eTX1jkXlsTXHeux_f2o859IB_&ua=tvproj&uipk=5&nbs=1&deadline=1622289611&gen=playurlv2&os=kodobv&oi=2078815810&trid=b7708dc7ef174e5bbe4fba32f5418517t&upsig=29cbb17759b52b6499638195bf0861aa&uparams=e,ua,uipk,nbs,deadline,gen,os,oi,trid&mid=474403243&bvc=vod&orderid=0,1&logo=80000000" "D:\BiliDownload\快住手！这根本不是 Kawaii Bass！_ 恋のうた Remix 工程演示.mp4"
   ```
   程序会在下载完成之后直接退出。

3. 输入一个 AV 号或 BV 号。

4. **[如果从未保存过 SESSDATA 或 TOKEN，或者在保存的登录凭据中存在无法凭之成功登录的]** 输入所选登录方式的编号。

5. **[如果选择了登录方式并选择 WEB 端二维码登录或 TV 端二维码登录]** 打开标题为 `WEB 端二维码登录` 或  `TV 端二维码登录` 的窗口，使用B站手机客户端扫码并确认登录。程序每隔一秒验证一次扫码是否完毕。

6. **[如果选择了登录方式并选择输入 SESSDATA 登录]** 输入 cookie 中 SESSDATA 的值。

7. **[如果选择了登录方式并登录成功]** 输入 `Y` 或 `N` 决定是否保存 SESSDATA 或 TOKEN。

8. **[如果选择了登录方式]** 输入 `Y` 或 `N` 决定是否继续登录。

9. 等待程序获取稿件信息。程序会返回稿件的标题、UP主、时长、播放数、弹幕数、获赞数、投币数以及收藏数。若该视频有多个分P，则会一并返回每个分P的 CID、时长与标题。

10. **[如果稿件有多个分P]** 输入所需分P的编号。

11. 等待程序获取清晰度信息。

12. 输入所需清晰度的编号。

13. **[如果从未保存过保存路径或保存的路径不存在]** 输入保存路径。

14. **[如果手动输入了保存路径且该路径不存在]** 输入 `Y` 或 `N` 决定是否新建目录。

15. **[如果手动输入了保存路径]** 输入 `Y` 或 `N` 决定是否保存该保存路径。

16. 输入所选下载选项的编号。

17. **[如果选择 `视频+音频` 下载选项，且从未保存过 FFmpeg 路径或路径不存在]** 输入 `ffmpeg.exe` 所处目录的路径。若填 `#` 则程序将不再进行音视频合并操作。

18. **[如果手动输入了 FFmpeg 路径]** 输入 `Y` 或 `N` 决定是否保存 FFmpeg 路径。

19. **[如果文件大小大于或等于 8MB 且从未保存过下载所用线程数]** 输入下载所用线程数。输入数字的最大值不定，且过大易发生代码为416的请求错误。

20. 等待下载完毕。下载完成后，若需合并则会生成合并文件并删除源文件。

---

# 补充

1. 输入 AV 号或 BV 号时，须带有 `av` 或 `BV` 前缀。

2. 如需下载 1080P+ 及更佳清晰度的视频，请确保您的账号已购买大会员或电视大会员。

3. 获取视频的清晰度信息时会访问 TV 端 API 与 WEB 端 API。若通过 TV 端 API 可获取到无水印版本，则所得清晰度会优先排列在结果中，并添加 `无水印` 标记。否则，将只保留通过 WEB 端 API
   所得的清晰度。注意，由于有无水印是根据 TV 端 API 提供的 `accept_watermark` 判断的，通过 WEB 端 API 获取的清晰度将一律不标记 `无水印`。这意味着没有 `无水印` 标记的清晰度不一定有水印。

4. 在任一个步骤输入时，输入 `*exit` 会使程序结束运行。

5. 对于上述使用流程中的所有输入，均可写入工作目录下的 `Input.txt` 中。

6. **[BUG]** 在某些控制台中，实时更新的下载详细信息会无法正常显示。例如下载完毕后速度信息的一部分被用时信息遮盖。

7. **[BUG]** 在第`n`次重试下载时，即使进度达到了100%也不会停止，而是待10s内平均速度为零后进行第`n+1`次重试，并且立即下载完毕。

8. **[BUG]** 在下载时，即使有数据正在传输，瞬时速度也为零。

9. **[BUG]** 在调用 FFmpeg 合并时，文件大小达到一定值（4GB左右）将不再写入。

10. **[BUG]** 获取无水印源时有时会失败。

11. 本程序仍然存在诸多问题，欢迎大家多多投递 issue 与 pull request。

---

# ChangeLog

## 1.3.6

### 1. 添加了路径开头“~”的解析功能 [#6](https://github.com/Naptie/BiliDownload/issues/6)

当路径以“~”开头时，会自动将其替换为用户主目录。

### 2. 解决了 Linux / MacOS 上 FFmpeg 可执行文件的名称问题 [#7](https://github.com/Naptie/BiliDownload/issues/7)

在 Linux / MacOS 等平台上，可执行文件名一般不含 `.exe`。

## 1.3.5

### 1. 添加了下载重试功能

当 10s 内平均速度达到零时，程序会中断下载并重试。

### 2. 详细了速度信息

之前的 `速度` 更名为 `平均速度`；添加了 `瞬时速度`，指 0.5s 内的平均速度。

### 3. 添加了直接下载功能

直接在参数中输入 `direct "<url>" "<path>"` 即可下载指定 URL 中的文件。

## 1.3.2

### 解决了如图所示的问题

<img src="bin/arithmetic_exception.png" alt="">

## 1.3.1

### 1. 在二维码中添加了 LOGO

在生成 WEB 端二维码时会在其中心添加 `resources` 中的 `logo-super-ellipse-resized.png` 图片，在生成 TV
端二维码时会在其中心添加 `logo-super-ellipse-2-resized.png` 图片。

### 2. 自定义化了多线程下载所用线程数

如果文件大小大于或等于 8MB 且从未保存过下载所用线程数，程序将会询问下载所用线程数。

## 1.3.0

### 1. 加速了下载

使用32个线程进行视频与音频的下载，最高速度可达 23MB/s。

### 2. 添加了 LOGO

现在，BiliDownload 也有了自己的 LOGO。原图存于 `bin/logo.png`。

### 3. 添加了 WEB 端二维码登录时浏览器标识的提示信息

选择 `WEB 端二维码登录` 并触发程序进行验证后，程序会提示当前所用的浏览器标识，便于与B站的登录操作通知中的 `设备/平台` 信息进行核实对照。

### 4. 解决了文件路径不能含有空格的问题

现已支持输入的文件路径中含有空格。

## 1.2.0

### 添加了 TV 端二维码登录功能

目前有三种登录方式可选：`WEB 端二维码登录`、 `TV 端二维码登录` 与 `输入 SESSDATA 登录`。若希望双端皆处于登录状态，在登录某端后继续登录另一端即可。

## 1.1.9

### 添加了 WEB 端二维码登录功能

目前有两种登录方式可选：`WEB 端二维码登录` 与 `输入 SESSDATA 登录`。`WEB 端二维码登录` 与 `TV 端二维码登录` 的区别在于，前者调用 WEB 端的 API 进行登录，在调用 WEB 端 API
下载视频时将解锁高清晰度；后者调用 TV 端的 API 进行登录，在调用 TV 端 API 下载视频时将解锁高清晰度。

## 1.1.7

### 自定义化了暂时存储路径

若选择 `视频+音频（合并需要 FFmpeg）` 并输入了 FFmpeg 路径，则程序在保存 `tmpVid.mp4` 与 `tmpAud.aac` 时将不再保存至 `System.getProperty("user.dir")`
获取的默认路径中，而将保存至指定的保存路径中。

## 1.1.6

### 1. 添加了退出功能

在每次输入时，若输入 `*exit`，则程序会调用 `System.exit(0)` 中止进程。

### 2. 优化了代码结构

将主函数拆分成了多个函数，每一步为一个函数，增强了代码的可读性。

### 3. 优化了输出

若输入源为 `Input.txt` 文件，则程序在正常模式下将不再显示输入提示，在 `debug` 模式下会一并输出所读取的内容。

## 1.1.5

### 1. 添加了 SESSDATA、保存路径、FFmpeg 路径的记忆功能

现在您不再需要在每次运行程序时都输入 SESSDATA、保存路径与 FFmpeg 路径，这些都将在输入一次之后保存到工作文件夹下的 `config.yml` 中。

### 2. 添加了下载时显示速度与时间的功能

下载视频或音频时，程序除了显示进度，还会显示平均速度与剩余时间。下载完成后，会显示下载用时。

## 1.1.0

### 1. 解决了访问 URL 时拒绝连接的问题（[Issue #1](https://github.com/Naptie/BiliDownload/issues/1)）

导致这个问题的原因很简单，就是我在 `readUrl(String url, String cookie)` 方法中写了这样一行代码：

`Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1081));`。

这直接导致了程序在访问 URL 时选择了位于1081端口的代理。因此，如果这个端口没有代理，就会提示拒绝连接。

<img src="bin/your_debugging.jpg" alt="">

### 2. 添加了 Debug 模式

在参数中添加 `debug`，即可启用 debug 模式。在该模式下，程序每当访问一个 URL 时，就会输出所访问的 URL 以及所使用的 UA。其中，下载视频或音频时访问的 URL 将不被输出，因为这些 URL
已经通过 `成功获取...下载地址：...` 输出了。

## 1.0.0

### Initial Commit

---

# 无水印视频流接口的探索历程

#### 视频：[B站](https://www.bilibili.com/video/BV1pK4y1N7gw)；[YouTube](https://youtu.be/ackdugNrRBc)。

- 0

3月31日的深夜，我无法安眠。

似有什么东西，撑着我的眼皮，愣是不让我关闭眼睛线程。

无奈之下，我起床走向客厅，打开电视，看起了B站。

无意间，看到了非常眼熟的画面，仿佛在某时某处看过一样。

我急忙打开手机，找到了这支视频，真相就在眼前——

**我为它进行了一键式三重连接。**

- 1

比起这一些，更引人注目的是，

电视上画面右上角少了一些奇怪的东西。

我赶忙用手机拍下来，所幸的是成功将这一似视觉欺骗般的幻象捕捉到了。

为了扩大样本容量，我将图片转发给更多的人，以确保这个现象是铁打的事实。

最终，所有人都观测到了这个现象，否定了我的视神经对于电视上这一特定的像素排列存在特异性的可能。

于是，我大胆推测，

之所以产生这一现象，

**是因为两个客户端分别使用了不同的端口。**

- 2

不知什么时候，我闭上了眼，被一股神秘的力量。

再睁眼看这世界，看到了天花板。

身体受到的压强远低于感知范围，就像躺在床上一样。

**笑死，我就在床上。**

- 3

4月，API 之月。

学业如此繁忙，却总有时间研究这个课题。

我若资本家，压榨着时间的剩余价值。

殊不知，时间是我的鸽命。

我于是再次打开远程连接，趁 OI 教练 AFK 之时，

安装 BlueStacks，

安装 Wireshark，

安装云视听小电视。

殊不知，Wireshark 对于我而言为时尚早。

我于是继续尝试，

安装 Charles。

又殊不知，这玩意他喵的看不到B站的 Path。

我于是继续尝试，

安装 Fiddler。

双殊不知，Fiddler 在默认情况下无法检测到 BlueStacks 的连接。

我于是继续尝试，

安装 Proxifier，

安装 ProxyCap。

叒殊不知，我的技术力过低而导致代理了个寂寞。

我于是继续尝试······

在手机上安装云视听小电视，并使手机的网络连接通过 Fiddler······

叕殊不知，

**安卓系统不再信任用户证书。**

- 4

我遇到了前所未有的尴尬局面。

艰难的决定······

「果然还是要这样了吗。」

带着一丝慌张，我找到了已4096个小时没有开机的华为 P7，

却万万没想到，

“getDuration(Action.BOOT_UP)”的返回值，

**是一夜。**

- 5

时间在消磨我的意志，好奇却在培育它。

KingRoot，安装。

RE 浏览器，安装。

云视听小电视，安装。

**我深知，前方是光明。**

- 6

万事俱备，只欠代理。

“192.168.3.149”“8888”

云视听小电视，打开。

Fiddler 还是带来了希望。

我深知，每当我掠过一个请求，

**真相就会更大概率地降临。**

- 7

不负众望，

通往无水印世界的大门终被打开，

那一似视觉欺骗般的幻象，

**将被无限重生。**

<img src="bin/example.png" alt="">
