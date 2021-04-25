Armadeus TODOs
==============

Long Term: Admadeus 2.0
-----------------------

- Web based dashboard for guild configuration
- __Use Night-Config for flat-file configurations (Velocity)__
- __Create an EventManager/Scheduler for use with event waiters (Velocity)__
- Re-Implement Command Cooldowns
- Group based command modules to be enabled per guild  
- Command Permissions & Documentation
- Create Help command
- Module based extensions
    - Audio
        - __Create Module__
        - Only query songs once they are next to play, save on requests
        - Write new Radio system
      - ImagesBoards
        - Create Module
        - Caching on a key to prevent duplicates, potentially expiring
        - Register NSFW command group
        - Register non NSFW command group
    
- Inter-process communication via redis
  - register ID in armadeus.instances
  - Add guild dev_enabled flag
    - If dev-instance is online, normal nodes will ignore all commands from said guild