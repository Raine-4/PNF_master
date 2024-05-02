# PNF Master 

## Language 

- [简体中文](#简体中文)
- [English](#english)

---

### 简体中文

---

## 1  应用介绍

北航（现在已经没了的）国际通用工程学院机械专业的个人毕业设计。团队毕设是制作一个基于PNF疗法的下肢肌肉疲劳预防机器人，我个人负责的部分是开发一个Android应用。
**基础功能**：通过蓝牙控制机器人（上的电机）实现患者自主训练。
**拓展功能**：拓展功能必须注册账号才可以使用。内置名为“PNF Master”的人工智能助手，可以根据用户输入的个人资料来智能推荐训练参数并生成训练曲线。所有数据根据用户账号在不同设备同步，不保存在本地。

## 2 用户使用帮助

1. **基础功能**：首先，用户需要点击右上角的蓝牙按钮连接设备。之后在操控页面点击连接电机按钮并选择电机参数，点击启动电机即可开始训练。
2. **拓展功能**：用户可以通过右滑屏幕或者点击左上角菜单按钮呼出菜单。点击“我的资料”可以查看并编辑用户的个人信息和医疗信息；点击“我的参数”可以查看当前和历史参数，并且可以添加自定义参数或AI推荐参数。如果有更多问题，点击操控页面右下角的AI按钮来询问PNF Master哦。

## 3 开发者帮助

1. **开发语言**：Kotlin + java (只有AIAssistant.java是用java写的)
2. **IDE**：Android Studio Iguana
3. **AI接口**：调用的是百度的文心一言。在调接口的`AIAssistant.java`中，修改`getAccessToken()`的`body`变量中的`BuildConfig.API_KEY`和`BuildConfig.SECRET_KEY`参数即可换成你自己的接口；修改`GetAnswer()`方法的`body`变量的`system`参数可以修改AI的人设。
4. **数据库**：Mysql。使用腾讯云的学生优惠数据库；数据库管理软件使用Navicat。源代码中所有数据库操作都在connect.kt文件中，每个功能对应一个方法，并且已经按照增删改查分类好了。
5. **其他**：因为应用比较简单（作者从零开始只学了几个月），所以没有加入MVVM等框架。另外Fragment用的也比较少，因此有很多Activity，但是逻辑其实很清楚。如果有什么问题，欢迎随时来骚扰作者。

---
### English
---

## 1 Introduction

A graduation project in Mechanical Engineering at Beihang University. The project is part of a team project to create a lower limb muscle fatigue prevention robot based on Proprioceptive Neuromuscular Facilitation (PNF) therapy. My personal responsibility is to develop an Android application.
**Basic Features**：Control the robot (and its motors) via Bluetooth to enable patients to train autonomously.
**Extended Features**：Extended features require account registration. Includes an AI assistant named "PNF Master" that intelligently recommends training parameters and generates training curves based on user input personal data. All data is synchronized across different devices based on user accounts and is not saved locally.

## 2 Guide for Users

1. **Basic Features**：Firstly, users need to click the Bluetooth button in the top right corner to connect to the device. Then, on the control page, click the 'Connect Motor' button and select motor parameters, then click 'Start Motor' to begin training.
2. **Extended Features**：Users can swipe right on the screen or click the menu button in the top left corner to bring up the menu. Clicking "My Profile" allows users to view and edit their personal and medical information; clicking "My Parameters" allows users to view current and historical parameters, and can add custom parameters or AI-recommended parameters. For further assistance, click the AI button in the bottom right corner of the control page to ask PNF Master.

## 3 Guide for Developers

1. **Programming Language**：Kotlin + java (only AIAssistant.java is written in Java)
2. **IDE**：Android Studio Iguana
3. **AI API**：Utilizes Baidu's ERINE Bot. In the `AIAssistant.java` file where the interface is called, modify the `BuildConfig.API_KEY` and `BuildConfig.SECRET_KEY` parameters in the `getAccessToken()` method's `body` variable with your own API key and secret key; modify the `system` parameter in the `GetAnswer()` method's `body` variable to change the AI persona.
4. **Database**：MySQL. Utilizes Tencent Cloud's student discount database; Navicat is used for database management. All database operations in the source code are in the `connect.kt` file, with each functionality corresponding to a method, categorized by CRUD operations.
5. **Others**：Because the application is relatively simple (the author only studied for a few months from scratch), frameworks like MVVM are not included. Additionally, Fragments are also used sparingly, resulting in many Activities, but the logic is clear. If there are any questions, feel free to harass the author at any time."