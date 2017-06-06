## Description ##

A simple multi-content provider process to generate IP/UDP packets for interfacing with GNU Radio. 

### Implementation Notes ###
Content generation is controlled through a Markov Process defined in ```StateMachine```

Specify content providers with json file ```contentDescriptors.json```

Each content provider runs in its own thread and can be configured to send IP/UDP packets to seperate destinations

Configuration option enables content encapsulation with custom formed IP/UDP header

### Dependencies ###
- gson 2.6.2
- guava 21.0
- Apache commons-math3-3.6.1
- user provided content files
