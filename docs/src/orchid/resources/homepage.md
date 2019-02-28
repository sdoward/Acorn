---
extraCss:
    - |
        inline:.scss:
        .image-preview {
            text-align: center; 
            img {
                max-width:480px;
            }    
        }
---

Acorn is a carefully designed library that brings true modularity to 
navigation flow and allows you to have full control over your transition
animations.

Acorn's main principles lie around two interfaces: The 
{{ anchor('Scene') }} and the {{ anchor('Navigator') }}.
The Scene represents a screen in an application and can be regarded 
as a basic building block for the application flow.
The Navigator controls the application flow and determines which screen
is presented to the user.

![]({{ 'media/class_diagram.png'|asset }})
{.image-preview}

A Scene class can be viewed as the simplest form of what a screen can be.
It has a simple four-staged lifecycle and is immune to Activity changes,
meaning it will not get destroyed when the user rotates the device.
Views get attached to Scenes when they become visible to the user, providing a
clear boundary between the platform and any logic.


The Navigator is completely free to choose how it is implemented.
It does not necessarily have to use a back-stack like structure to model the 
application flow, but can use any data structure they like.
In fact, Navigators can be composed together to combine several sub-flows
into one major application flow.

In Acorn, the Activity is regarded as a 'window' to the user.
Its only responsibility is to react to screen changes and show the proper
user interface. 
This decoupling of navigation and the user interface results in an excellent
way to do transition animations: whenever the screen changes you get full
control over the root ViewGroup in the Activity, allowing you to do anything
you want.

In the {{ anchor('Wiki') }} section you can find information on several topics 
when working with Acorn.

_Note: this documentation website is work in progress, and some sections may be
missing or incomplete._
