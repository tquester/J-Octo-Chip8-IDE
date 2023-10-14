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

# language extensions

structs define an alias for registers with some special functions. You can access members of a struct by Name.member, for example Player.x or inside a with statement. A struct also simplyfies storage. Player.byte creates room for the struct, depending on how many registers have been used. Also members can be set. The Struct name itself compiles to the last register, so save Player will compile to save v3.

switch..case simplifies comparision for keys or registers.  If you start a switch with a register, only one case will be executed, if you start with switch key all cases will be tried, there can be more than one key pressed at the same time. switch key will use the vf register for key comparision.

Loops can be done with for..next..step like in Basic. for v1 := 1 to 10 begin ... end or for v1 := 1 to 10 step 2 begin end. The comparison at the end must match, for v1 := 1 to 10 step 3 will not reach the 10 and may run forever. It is allowed to set the for variable inside the loop.

:tileset, :sprite and :tile define data and a label, but the commands allow the sprite editor and the tile editor to read and modify the data

Sample:



          :struct Player {                                            # define our local variables
              x                                                       # v0
              y                                                       # v1
              oldx                                                    # v2
              oldy                                                    # v3
            }

            i := playerInit                                           # load init data
            load Player

            with Player {                                             # within the block we can use x,y etc.
              i := ball                                               # outside write Player.x
              sprite x y 8                                            # initial draw the ball
              loop
                oldx := x
                oldy := y
                switch key begin
                  case 1 x += 1                                       # move sprite without limit
                  case 2 x -= 1                                       # testing short instruction
                  case 3 y -= 1                                       # uses skip if not
                  case 0xc y += 1

                  case 7 begin                                        # move sprite within limits
                    if x > 0 then  x -= 1                             # testing multiple commands
                  end                                                 # using skip if

                  case 9 begin
                    if x < 56 then x += 1
                  end

                  case 0 begin
                    if y < 24 then y += 1
                  end

                  case 5 begin
                    if y > 0 then y -= 1
                  end
                end
                vf := 0                                               # lets see if the sprite
                if x != oldx then vf := 1                             # has moved
                if y != oldy then vf := 1
                if vf == 1 begin                                      # only redraw if moved
                  i := ball
                  sprite oldx oldy 8
                  i := ball
                  sprite x y 8
                end
                vf := 2
                delay := vf
                loop
                  vf := delay
                  while vf != 0
                again
              again
          }
          : playerInit
            Player.byte {                                             # this creates 4 bytes
              x := 10                                                 # the remaining registers will
              y := 15                                                 # be initilized with 0
            }
            
          :sprite ball,8,8 { # ensure the sprite editor can find this sprite and edit it with the correct size
              0x18 0x3c 0x7e 0xff 0xff 0x7e 0x3c 0x18
            }


