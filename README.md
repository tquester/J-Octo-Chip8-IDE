# j-octo
IDE for Chip8-Game development: Assembler, Emulator, Sprite-Editor, Disassembler
This is an early release, there is lots of work to do, still.

The IDE contains an editor, compiler, debugger, disassembler and a sprite editor.
The compiler accepts the octo langugage as well as the chipper assembly language.

F12 in the editor hyper jumps to a label. Alt-Left returns.
Ctrl-Space opens the auto completion window

![image](https://github.com/tquester/j-octo/assets/5380723/311d6add-7310-43a3-a20d-10326bdc4c55)

The debugger/emulator displays the disassembled code. If the game has been started from the editor, it will contain all symbols, except for the symbols the compiler inserted for if/begin/end/loop/again and while. The data view always shows the data pointed by the I register and decodes binary data into visible sprites

![image](https://github.com/tquester/j-octo/assets/5380723/e48b23f7-8df4-4779-8ef0-dfb46b1e5661)

The disassembler separates code from data and lets you rename the labels and change the type, for example to visualize sprites or 16x16 sprites

![image](https://github.com/tquester/j-octo/assets/5380723/29898af4-14ac-4f71-9a75-c1b20c995a6d)

The sprite editor can read the sprites from your source code if you add a comment. It lets you edit a group of sprites, copy a sprite, flip and shift to help you creating animations.

 tiles: 8x8
 sprite: 8x8
 tiles: 16x16
![image](https://github.com/tquester/j-octo/assets/5380723/45f8888b-a0c5-43df-8de8-0e5a67519fde)

Warning: You must copy the sprites back to your source code with copy/paste, the sprite editor will not modify the source code for now.


