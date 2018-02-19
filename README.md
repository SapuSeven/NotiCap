# NotiCap
[![Build Status](https://travis-ci.org/SapuSeven/NotiCap.svg?branch=master)](https://travis-ci.org/SapuSeven/NotiCap)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d4b46205975f4767ae1cae9b42a4b0f0)](https://www.codacy.com/app/SapuSeven/NotiCap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=SapuSeven/NotiCap&amp;utm_campaign=Badge_Grade)

NotiCap executes SSH commands when receiving a notification from a specific application.

## Usage

After installation, you first have to grant NotiCap notification access.

To do this, go to _Filter Rules->Setup_, search for NotiCap in the displayed list and enable notification access.

The next step is to setup your first identity at _SSH Identities->Add SSH Identity_.

Finally, go back to _Filter Rules_ and select _Add Filter Rule_.

_Note: The Filter Rule screen only provides very basic options at the moment. If you miss a specific function, feel free to add it and submit a Pull Request or open a new issue._
