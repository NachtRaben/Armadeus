Armadeus TODOs
==============

Long Term: Admadeus 2.0
-----------------------

- Web based dashboard for guild configuration
- __Use Night-Config for flat-file configurations (Velocity)__
- __Create an EventManager/Scheduler for use with event waiters (Velocity)__
- Module based extensions
    - Audio
        - __Create Module__
        - Only query songs once they are next to play, save on requests
        - Write new Radio system
      - ImagesBoards
        - Caching on a key to prevent duplicates, potentially expiring
- Command Categories
  - Break commands into categories that can be enabled/disabled per guild
    - Moderation
    - Admin
    - Images
    - NSFW

Short Term: Armadeus 1.0
------------------------

- Re-Implement Command Cooldowns
- __Re-implement Command Blacklist__
- __Fix volume and equalizer settings__
- Rewrite audio subsystem
- Create Help command
- Convert Moderation commands  
- Convert non NSFW image commands
  - birb
  - nekolife
- Command Permissions
- Database configuration conversion
- Inter-process communication via redis
  - register ID in armadeus.instances
  - Add guild dev_enabled flag
    - If dev-instance is online, normal nodes will ignore all commands from said guild