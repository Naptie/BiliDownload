<h1 align="center">BiliDownload</h1>

<h5 align="center">本项目通过调用B站 WEB 端与 TV 端的 API 实现了对B站视频的下载。</h5>

---

# 声明

1. 本项目遵循 MIT 协议，修改/发布前须经本人同意;

2. 本项目仅用于学习和测试，请勿滥用;

3. 因使用本项目提供的服务而造成的后果与本人无关；

4. 由于本项目较为特殊，不排除随时删档的可能性。

---

# 使用前

1. 请确保您已安装 [JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) 或更高版本。

2. 如需使用音视频合并功能，请确保您已安装 [FFmpeg](http://www.ffmpeg.org)。

---

# 使用流程

0. （可跳过）使用 `mvn clean package` 命令进行编译。

1. 输入 `java -jar bili-download-1.1.0-jar-with-dependencies.jar`，进入程序。若您希望启用 debug 模式，请在参数中添加 `debug`，即输入 `java -jar bili-download-1.1.0-jar-with-dependencies.jar debug`，这将显示程序所连接的网址以及所使用的 UA。

2. 输入一个 AV 号或 BV 号。（须带有 `av` 或 `BV` 前缀）

3. 输入 Cookie 中 SESSDATA 的值（若无则填“#”）。如需下载 1080P+ 及更佳清晰度的视频，请确保您的账号已购买大会员。（暂不支持 4K 与 1080P+ 清晰度的无水印下载）

4. 等待程序获取稿件信息。程序会返回稿件的标题、UP主、时长、播放数、弹幕数、获赞数、投币数以及收藏数。若该视频有多个分P，则会一并返回每个分P的 CID、时长与标题。

5. 如果稿件有多个分P，输入所需分P的编号。

6. 等待程序获取清晰度信息。执行这一步时会访问 TV 端 API 与 WEB 端 API ，若通过 TV 端 API 可获取到无水印版本，则所得清晰度会优先排列在结果中，并添加 `无水印` 标记。否则，将只保留通过 WEB 端 API 所得的清晰度。注意，没有 `无水印` 标记的清晰度不一定有水印。（貌似 TV 端 API 无需登录即可解锁 1080P 清晰度，而如果没有填写 SESSDATA 且所解析的视频不存在无水印版本则将丢失原本应能获取到的 1080P 下载地址）

7. 输入所需清晰度的编号。

8. 输入保存目录。（目录须存在）

9. 输入所选下载选项的编号。

10. 如果选择 `视频+音频` 下载选项，输入 `ffmpeg.exe` 所处位置的路径，若填 `#` 则程序将不再进行音视频合并操作。

11. 等待下载完毕。下载完成后，若需合并则会生成合并文件并删除源文件。（如果速度慢，请自行打开下载地址进行下载）

---

# 补充

1. 本程序仍然存在诸如 Maven 编译配置不恰当、不支持 4K 与 1080P+ 清晰度的无水印下载、下载速度慢等问题，欢迎大家多多 pr。

2. 对于上述使用流程中的所有输入，均可写入工作目录下的 `Input.txt` 中。

---

# ChangeLog

## 1.1.0
### 1. 解决了访问 URL 时拒绝连接的问题（[Issue #1](https://github.com/Naptie/BiliDownload/issues/1)）

导致这个问题的原因很简单，就是我在 `readUrl(String url, String cookie)` 方法中写了这样一行代码：
  
`Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1081));`。
  
这直接导致了程序在访问 URL 时选择了位于1081端口的代理。因此，如果这个端口没有代理，就会提示拒绝连接。

![image](https://github.com/Naptie/BiliDownload/blob/main/bin/your_debugging.jpg)

### 2. 添加了 Debug 模式

在参数中添加 `debug`，即可启用 debug 模式。在该模式下，程序每当访问一个 URL 时，就会输出所访问的 URL 以及所使用的 UA。其中，下载视频或音频时访问的 URL 将不被输出，因为这些 URL 已经通过 `成功获取...下载地址：...` 输出了。

## 1.0.0
### Initial Commit

---

# 无水印视频流接口的探索历程

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

![image](https://github.com/Naptie/BiliDownload/blob/main/bin/example.png)