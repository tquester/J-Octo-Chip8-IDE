# j-octo
IDE for Chip8-Game development: Assembler, Emulator, Sprite-Editor, Tile Editor, Disassembler
This is an early release, there is lots of work to do, still.

# installation

The git usually contains a runnable jar file for windows. After cloning you may be able to run it with double click.

If you want to compile the project:
1. download and install eclipse (https://www.eclipse.org/). Install eclipse for java developpers.
2. clone the project in a separate git directory (for example ~/dev/git/j-octo)
3. create a workspace (for example ~/dev/workspace/j-octo)
4. start eclipse and open the workspace
5. in eclipse, create a new java project. You may name it j-octo or anything you like
6. in eclipse, right click below the java project and and select import.
7. Now select: Import from git, existing repository. Now select the cloned repository and import it
8. If you run on linux or mac os: Open the pom.xml file and find the swt library. There are differnt libraries for windows, macos and linux.
9. Move the library for your os to the top
10. right click the pom.xml and select marven/update project

You should be able to run the project now.
If you want a jar-file right click the tree and select export/runnable jar file and follow the wizard.



# ide
The IDE contains an editor, compiler, debugger, disassembler and a sprite editor.
The compiler accepts the octo langugage as well as the chipper assembly language.

F12 in the editor hyper jumps to a label. Alt-Left returns.
Ctrl-Space opens the auto completion window

![image](https://github.com/tquester/J-Octo-Chip8-IDE/assets/5380723/dfee6310-c7fa-4c2b-a352-bc639d7e638e)


The debugger/emulator displays the disassembled code and the source code. You can see which macro is executed, hidden code (loop, again, while, if,..) and even dead code.
If the game has been started from the editor, it will contain all symbols, except for the symbols the compiler inserted for if/begin/end/loop/again and while. The data view always shows the data pointed by the I register and decodes binary data into visible sprites

![image](https://github.com/tquester/J-Octo-Chip8-IDE/assets/5380723/c9fdc705-ab8d-4585-828a-5d49003f4293)


The disassembler separates code from data and lets you rename the labels and change the type, for example to visualize sprites or 16x16 sprites

![image](https://github.com/tquester/j-octo/assets/5380723/29898af4-14ac-4f71-9a75-c1b20c995a6d)

The sprite editor can read the sprites from your source code if you add a comment. It lets you edit a group of sprites, copy a sprite, flip and shift to help you creating animations.

 tiles: 8x8
 sprite: 8x8
 tiles: 16x16
![image](https://github.com/tquester/j-octo/assets/5380723/45f8888b-a0c5-43df-8de8-0e5a67519fde)

Warning: You must copy the sprites back to your source code with copy/paste, the sprite editor will not modify the source code for now.


