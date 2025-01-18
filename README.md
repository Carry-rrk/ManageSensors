# ManageSensors
[中文版本](./README-zhCN.md)
## Table of Contents
- [Project Overview](#project-overview)
- [Current Status](#current-status)
- [Features](#features)
- [Usage Instructions](#usage-instructions)
- [Known Issues](#known-issues)
- [Special Notes](#special-notes)
- [Contribution](#contribution)
- [License](#license)

## Project Overview
**ManageSensors** is an Android application based on **Shizuku** that enables fine-grained control over app permissions through **AppOps**, specifically addressing the issue of certain apps still triggering vibrations in silent or Do Not Disturb mode.

## Current Status
### Test Environment
- **Shizuku** (via ADB or root access)
- **Android 15**
- **Realme Devices** (OS Version: Realme UI 6.0)

### Test Results
- Some **AppOps** settings may not take effect.
- The primary goal (disabling vibrations in silent/Do Not Disturb mode) has been successfully achieved.
- The OS version (Realme UI 6.0) has minimal impact on functionality, and it is expected to work on other Android 15-based devices as well.

## Features
- Utilizes **Shizuku** to call **AppOps** APIs for fine-grained app permission control.
- Resolves the issue of apps vibrating in silent/Do Not Disturb mode, improving user experience.
- Provides a clean and simple user interface for easy app permission management.

## Usage Instructions
1. Ensure that **Shizuku** is installed and running on your device (refer to the [Shizuku Documentation](https://github.com/RikkaApps/Shizuku) for details).
2. Download and install **ManageSensors**.
3. Open the app and grant **Shizuku** permissions.
4. Select the target app from the list and adjust the relevant permission settings.

## Known Issues
- Some **AppOps** settings may not take effect, possibly due to system restrictions or device compatibility.
- Currently tested only on **Android 15** and **Realme Devices** (Realme UI 6.0); compatibility issues may exist on other devices and Android versions.

## Special Notes
- As a non-professional Android developer, the majority of the code was generated using **Cursor**, an AI-powered code generation tool. If there are any copyright issues, please raise an Issue, and I will address it promptly.
- Although the testing environment is limited (Realme devices, Realme UI 6.0), it is expected that this will not significantly impact functionality.

## Contribution
Issues and Pull Requests are welcome to help improve **ManageSensors**. When contributing, please note the following:
- When submitting an Issue, please provide a detailed description of the problem or suggestion.
- When submitting a Pull Request, ensure that the code style is consistent and include relevant test results.

## License
This project is licensed under the **MIT License**. For more details, please refer to the [LICENSE](LICENSE) file.