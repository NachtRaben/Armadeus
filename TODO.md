Armadeus TODOs
==============

Long Term: Admadeus 2.0
-----------------------

- Web based dashboard for guild configuration
- ~~Use Night-Config for flat-file configurations (Velocity)~~
- ~~Create an EventManager/Scheduler for use with event waiters (Velocity)~~
- Re-Implement Command Cooldowns
- Group based command modules to be enabled per guild  
- ~~Command Permissions~~ & Documentation
- Implement logging groups for moderation/tracking
    - break down into guild specific logging channels for types
- Create Help command
- ~~Module based extensions~~
    - Audio
        - ~~Create Module~~
        - Only query songs once they are next to play, save on requests
        - ~~Write new Radio system~~
        - Song history COmmand 
      - ImagesBoards
        - Create Module
        - Caching on a key to prevent duplicates, potentially expiring
        - Register NSFW command group
        - Register non NSFW command group
    
- ~~Inter-process communication via redis~~
  - ~~register ID in armadeus.instances~~
  - ~~Add guild dev_enabled flag~~
    - ~~If dev-instance is online, normal nodes will ignore all commands from said guild~~
