# PNF Master 

## 1  应用介绍 Introduction

北航（现在已经没了的）国通学院机械专业的个人毕业设计。团队毕设是制作一个基于PNF疗法的下肢康复机器人，我个人负责的部分是开发一个Android应用，基本功能是能通过蓝牙控制机器人（上的电机）实现患者自主康复训练。拓展功能计划包括云端存储用户数据，同一账户可以在不同设备上登录，以获得相同的个人资料。而个人资料可以作为程序为患者定制个性化康复计划的依据，这个功能之后可能会接入一个AI的API来实现。

A graduation project in Mechanical Engineering at Beihang University. The project is part of a team project to create a lower limb rehabilitation robot based on the Proprioceptive Neuromuscular Facilitation (PNF) therapy. My specific responsibility is to build an Android app that allows patients to control the robot's motors via Bluetooth, enabling autonomous rehabilitation training. In addition to the basic functionality, the app may be implemented some advanced features such as cloud storage for user data. Users will be able to log in to the same account from different devices, ensuring consistent access to their personal profiles. These profiles will serve as the basis for customizing personalized rehabilitation plans for patients. Furthermore, the idea of integrating an AI component to enhance the system's capabilities in the future is being considered.

## 2 Note

### 2.1 需要联网使用的功能

1. LoginActivity：点击登录按钮之后需要等待一秒钟；
2. NewUserActivity：点击下一步时需要检测用户名是否已存在，需要等待一秒；
3. RehabInfoActivity：上传至数据库；
4. ProfileActivity：打开的时候需要调取数据库的信息；

### 2.2 待加入的功能

1. 定时发送通知，提示用还未完成今天的任务（当然也可以在设置里关闭）
2. 可以通过点击头像来更换头像，头像需要存储至云端数据库
3. 其实可以考虑一下云端数据和本地数据库并存，这样就可以仅第一次打开较为耗时，之后打开直接从本地数据库读取，速度会快非常多。
4. 加入中英双语两个版本。