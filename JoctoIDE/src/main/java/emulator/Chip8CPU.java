package emulator;

import java.security.spec.MGF1ParameterSpec;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import assembler.CDebugEntries;
import assembler.CDebugEntry;
import assembler.CDebugEntry.CDebugElem;

public class Chip8CPU {
	public byte memory[] = new byte[65536];
	public CDebugEntries mDebugEntries = null;
	public IEmulatorCallback mEmulatorCallback;
	public int pc = 0x200;
	public int vx[] = new int[16];
	public int regI;
	public byte keyPressed[] = new byte[16];
	Stack<Integer> stack = new Stack<>();
	public int regDelay;
	public int regSound;
	HashSet<Integer> mBreakpoints = new HashSet();
	public Chip8GPU gpu = new Chip8GPU();
	public Chip8Debugger debugger = new Chip8Debugger();
	private boolean mStop=false;
	public boolean mRunning;
	private boolean mNewShift = false;
	private boolean mNewLoad = false;
	//int mBreakpoints[] = new int[10];
	//int mNumbBreakpoints = 0;

	int[] font5 = { 0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
			0x20, 0x60, 0x20, 0x20, 0x70, // 1
			0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
			0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
			0x90, 0x90, 0xF0, 0x10, 0x10, // 4
			0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
			0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
			0xF0, 0x10, 0x20, 0x40, 0x40, // 7
			0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
			0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
			0xF0, 0x90, 0xF0, 0x90, 0x90, // A
			0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
			0xF0, 0x80, 0x80, 0x80, 0xF0, // C
			0xE0, 0x90, 0x90, 0x90, 0xE0, // D
			0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
			0xF0, 0x80, 0xF0, 0x80, 0x80 // F

	};

	int[] bigfont = { 0x00, 0x18, 0x24, 0x42, 0x42, 0x42, 0x42, 0x24, 0x18, 0x00, // 0
			0x00, 0x08, 0x18, 0x28, 0x08, 0x08, 0x08, 0x08, 0x3E, 0x00, // 1
			0x00, 0x3C, 0x42, 0x02, 0x04, 0x18, 0x20, 0x40, 0x7E, 0x00, // 2
			0x00, 0x3C, 0x42, 0x02, 0x0C, 0x02, 0x02, 0x42, 0x3C, 0x00, // 3
			0x00, 0x0C, 0x14, 0x14, 0x24, 0x24, 0x44, 0x7E, 0x04, 0x00, // 4
			0x00, 0x7E, 0x40, 0x70, 0x0C, 0x02, 0x02, 0x46, 0x38, 0x00, // 5
			0x00, 0x1C, 0x62, 0x40, 0x40, 0x7C, 0x42, 0x42, 0x3C, 0x00, // 6
			0x00, 0x3E, 0x02, 0x04, 0x04, 0x08, 0x08, 0x10, 0x10, 0x00, // 7
			0x00, 0x3C, 0x42, 0x42, 0x3C, 0x42, 0x42, 0x42, 0x3C, 0x00, // 8
			0x00, 0x3C, 0x42, 0x42, 0x3C, 0x02, 0x02, 0x42, 0x3C, 0x00, // 9
			0x00, 0x18, 0x24, 0x42, 0x42, 0x42, 0x7E, 0x42, 0x42, 0x00, // A
			0x00, 0x78, 0x44, 0x42, 0x42, 0x7C, 0x42, 0x42, 0x7C, 0x00, // B
			0x00, 0x38, 0x44, 0x40, 0x40, 0x40, 0x40, 0x44, 0x38, 0x00, // C
			0x00, 0x78, 0x44, 0x42, 0x42, 0x42, 0x42, 0x44, 0x78, 0x00, // D
			0x00, 0x7C, 0x40, 0x40, 0x78, 0x40, 0x40, 0x40, 0x7C, 0x00, // E
			0x00, 0x7C, 0x40, 0x40, 0x78, 0x40, 0x40, 0x40, 0x40, 0x00 // F
	};
	private int mSpeed = 5;

	public Chip8CPU() {
		gpu.memory = memory;
		debugger.cpu = this;
		for (int i = 0; i <= 15; i++)
			keyPressed[i] = 0;
		//for (int i = 0; i < 10; i++)
		//	mBreakpoints[i] = 0;
		//mNumbBreakpoints = 0;
		initFont();
		reset();
	}

	void initFont() {
		int i;
		int src = 10;
		for (i = 0; i < font5.length; i++) {
			memory[src + i] = (byte) (font5[i] & 0xff);
		}
		src = 110;
		for (i = 0; i < bigfont.length; i++) {
			memory[src + i] = (byte) (bigfont[i] & 0xff);
		}
	}

	public void stop() {
		if (mRunning) {
			mStop = true;

			for (int i = 0; i < 1000; i++) {
				try {
					if (!mRunning)
						break;
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void run() {
		Thread thread = new Thread() {

			@Override
			public void run() {
				runThread();
			}
		};
		thread.start();
	}

	void runThread() {
		mRunning = true;
		mStop = false;

		try {
			while (mStop == false) {
				for (int i = 0; i < mSpeed; i++) {
					if (mBreakpoints.contains(pc)) {
						mStop = true;
						break;
					}
					
					if (mDebugEntries != null) {
					   CDebugEntry entry = mDebugEntries.get(pc);
					   if (entry != null) {
						   if (entry.mIsBreakpoint) {
							   
							   mStop = true;
							   break;
						   }
						   if (entry.mLog != null) {
							   String log = "";
							   for (CDebugElem elem: entry.mLog) {
								   if (elem.register != -1) {
									   log += String.format("%02x (%d)",vx[elem.register], vx[elem.register]);
								   }
								   if (elem.text != null)
									   log += elem.text;
							   }
							   if (gpu.mIEmulator != null)
								   gpu.mIEmulator.log(log);
						   }
					   }
					}
				
					cpuTick();
				}
				try {
					Thread.sleep(1);
				} catch (Exception ex) {
	
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		if (gpu.mIEmulator != null)
			gpu.mIEmulator.notifyStop();
		mRunning = false;
	}

	public void cpuTick() {
		int i, a, b;
		int reg1, reg2;
		int ireg1, ireg2;
		int high, low, highnib, n;
		high = memory[pc++] & 0xff;
		low = memory[pc++] & 0xff;
		highnib = high & 0x0f;
		switch (high & 0xf0) {
		// 00Cn scroll screen content down N pixel, in XO-CHIP only selected bit planes
		// are scrolled [Quirk 1] [Quirk 2]
		// 00Dn scroll screen content up N hires pixel, in XO-CHIP only selected planes
		// are scrolled
		// 00E0 clear the screen, in XO-CHIP only selected bit planes are cleared, in
		// MegaChip mode it updates the visible screen before clearing the draw buffer
		// 00EE return from subroutine to address pulled from stack
		// 00FB scroll screen content right four pixel, in XO-CHIP only selected bit
		// planes are scrolled [Quirk 1]
		// 00FC scroll screen content left four pixel, in XO-CHIP only selected bit
		// planes are scrolled [Quirk 1]
		// 00FD exit interpreter
		// 00FE switch to lores mode (64x32) [Quirk 3]
		// 00FF switch to hires mode (128x64) [Quirk 3]
		case 0x00:
			switch(highnib) {
			case 0x01:
				regI = low << 16;
				int h = memory[pc] & 0xff;
				int l = memory[pc+1] & 0xff;
				regI += h * 256 + l;
				
				break;
			case 0x02:
				gpu.megaLoadPalette(this,low);
				break;
			case 0x03:
				
				gpu.megaSpriteH = low == 0 ? 256 : low;
				break;
			case 0x04:
				gpu.megaSpriteW = low == 0 ? 256 : low;
				break;
			case 0x05:
				gpu.megaAlpha = low;
				break;
			case 0x06:
				megaPlay(low & 0x0f);
				break;
			case 0x07:
				megaPlayStop();
				break;
			case 0x08:
				gpu.megaBlend = low;
				break;
				
				 
			default:
				switch (low) {
				case 0x10:
					gpu.megaOff();
					break;
				case 0x11:
					gpu.megaOn();
					break;
				case 0xE0:
					gpu.cls();
					break;
				case 0xFB:
					gpu.scrollRight();
					break;
				case 0xFC:
					gpu.scrollLeft();
					break;
				case 0xFE:
					gpu.lowres();
					break;
				case 0xFF:
					gpu.hires();
					break;
				case 0xFD:
					mStop = true;
					break;
				case 0xEE:
					pc = stack.pop().intValue();
					break;
				default:
					n = low & 0x0f;
					low &= 0xf0;
					switch (low) {
					case 0xC0:
						gpu.scrollDown(n);
						break;
					case 0xD0:
						gpu.scrollUp(n);
						break;
					default:
						invalidOpcode();
					}
				}
				break;

			
			}
			break;

		// 1nnn jump to address NNN
		case 0x10:
			pc = (highnib << 8) | low;
			break;

		// 2nnn push return address onto stack and call subroutine at address NNN
		case 0x20:
			stack.push(pc);
			pc = (highnib << 8) | low;
			break;

		// 3xnn skip next opcode if vX == NN (note: on platforms that have 4 byte
		// opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		case 0x30:
			if (expand(vx[highnib]) == expand(low))
				skip();
			break;

		// 4xnn skip next opcode if vX != NN (note: on platforms that have 4 byte
		// opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		case 0x40:
			if (expand(vx[highnib]) != expand(low))
				skip();
			break;

		// 5xy0 skip next opcode if vX == vY (note: on platforms that have 4 byte
		// opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		// 5xy2 write registers vX to vY to memory pointed to by I
		// 5xy3 load registers vX to vY from memory pointed to by I
		case 0x50:
			switch (low & 0x0f) {
			case 0x00:
				reg1 = expand(vx[highnib]);
				reg2 = expand(vx[low >> 4]);
				if (reg1 == reg2)
					skip();
				break;
			case 0x02:
				ireg2 = low >> 4;
				if (highnib <= ireg2) {
					for (i = highnib; i <= ireg2; i++)
						memory[regI++] = (byte) (vx[i] & 0xff);
				} else {
					for (i = highnib; i >= ireg2; i--)
						memory[regI++] = (byte) (vx[i] & 0xff);
				}
				break;
			case 0x03:
				ireg2 = low >> 4;
				if (highnib <= ireg2) {
					for (i = highnib; i <= ireg2; i++)
						vx[i] = memory[regI++];
				} else {
					for (i = highnib; i >= ireg2; i--)
						vx[i] = memory[regI++];
				}
				break;

			}
			break;

		// 6xnn set vX to NN
		case 0x60:
			vx[highnib] = low & 0xff;
			break;

		// 7xnn add NN to vX
		case 0x70:
			setvx(highnib, expand(vx[highnib]) + expand(low));
			// vx[highnib] = (expand(vx[highnib]) + expand(low)) & 0xff;
			break;

		// 8xy0 set vX to the value of vY
		// 8xy1 set vX to the result of bitwise vX OR vY [Quirk 4]
		// 8xy2 set vX to the result of bitwise vX AND vY [Quirk 4]
		// 8xy3 set vX to the result of bitwise vX XOR vY [Quirk 4]
		// 8xy4 add vY to vX, vF is set to 1 if an overflow happened, to 0 if not, even
		// if X=F!
		// 8xy5 subtract vY from vX, vF is set to 0 if an underflow happened, to 1 if
		// not, even if X=F!
		// 8xy6 set vX to vY and shift vX one bit to the right, set vF to the bit
		// shifted out, even if X=F! [Quirk 5]
		// 8xy7 set vX to the result of subtracting vX from vY, vF is set to 0 if an
		// underflow happened, to 1 if not, even if X=F!
		// 8xyE set vX to vY and shift vX one bit to the left, set vF to the bit shifted
		// out, even if X=F! [Quirk 5]
		case 0x80:
			reg2 = expand(vx[low >> 4]);
			switch (low & 0x0f) {
			case 0x00:
				setvx(highnib, reg2);
				// vx[highnib] = reg2 & 0xff;
				break;
			case 0x01:
				setvx(highnib, getvx(highnib) | reg2);
				// vx[highnib] |= reg2;
				break;
			case 0x02:
				setvx(highnib, getvx(highnib) & reg2);
				// vx[highnib] &= reg2;
				break;
			case 0x03:
				setvx(highnib, getvx(highnib) ^ reg2);
				// vx[highnib] ^= reg2;

				break;
			case 0x04:
				a = getvx(highnib) + expand(reg2);
				// a = expand(vx[highnib]) + expand(reg2);
				vx[highnib] = a & 0xff;
				vx[15] = a < 256 ? 0 : 1;
				break;
			case 0x05:
				a = getvx(highnib) - reg2;
				setvx(highnib, a & 0xff);
				vx[15] = a < 0 ? 0 : 1;
				// a = expand(vx[highnib]) - expand(reg2);
				// vx[highnib] = (byte)(a & 0xff);
				// vx[15] = a < 0 ? 0 : 1;
				break;
			case 0x06:
				
				b = mNewShift ? getvx(highnib) : reg2;
				a = b & 0x01;
				setvx(highnib, b >> 1);
				vx[15] = (byte) (a & 0xff);
				break;
			case 0x07:
				a = (reg2 - getvx(highnib)) & 0xff;
				
				setvx(highnib, a);
				vx[15] = a < 128 ? 1 : 0;
				// a = expand(reg2) - expand(vx[highnib]);
				// vx[highnib] = (byte)(a & 0xff);
				// vx[15] = a < 0 ? 0 : 1;
				break;
			case 0x0E:
				b = mNewShift ? vx[highnib] : reg2;
				a = b & 0x80;
				setvx(highnib, b << 1);
				vx[15] = a == 0 ? 0 : 1;
				break;
			}
			break;

		// 9xy0 skip next opcode if vX != vY (note: on platforms that have 4 byte
		// opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		case 0x90:
			reg2 = expand(vx[low >> 4]);
			if (expand(vx[highnib]) != reg2)
				skip();
			break;

		// Annn set I to NNN
		case 0xA0:
			regI = (highnib << 8) + low;
			break;

		// Bnnn jump to address NNN + v0
		// Bxnn jump to address XNN + vX
		case 0xB0:
			pc = (highnib << 8) + low + vx[0];
			break;

		// Cxnn set vx to a random value masked (bitwise AND) with NN
		case 0xC0: {
			double r = Math.random()*256;
			int temp = (int) (r);
			vx[highnib] = temp & low;
			//System.out.println(String.format("Random %d & %d = %d", temp, low, vx[highnib]));
			break;
		}

		// Dxyn draw 8xN pixel sprite at position vX, vY with data starting at the
		// address in I, I is not changed [Quirk 6] [Quirk 7] [Quirk 8]
		case 0xD0:
			a = gpu.draw(regI, vx[highnib], vx[low >> 4], low & 0x0f);
			vx[15] = a;
			break;

		// Ex9E skip next opcode if key in vX is pressed (note: on platforms that have 4
		// byte opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		// ExA1 skip next opcode if key in vX is not pressed (note: on platforms that
		// have 4 byte opcodes, like F000 on XO-CHIP, this needs to skip four bytes)
		case 0xE0:
			switch (low) {
			case 0x9e:
				if (keyPressed[vx[highnib]] == 1)
					skip();
				break;
			case 0xA1:
				if (keyPressed[vx[highnib]] == 0)
					skip();
				break;
			default:
				invalidOpcode();

			}
			break;

		// F000 assign next 16 bit word to i, and set PC behind it, this is a four byte
		// instruction (see note on skip instructions)
		// Fx01 select bit planes to draw on when drawing with Dxy0/Dxyn
		// F002 load 16 bytes audio pattern pointed to by I into audio pattern buffer
		// Fx07 set vX to the value of the delay timer
		// Fx0A wait for a key pressed and released and set vX to it, in megachip mode
		// it also updates the screen like clear
		// Fx15 set delay timer to vX
		// Fx18 set sound timer to vX, sound is played when sound timer is set greater 1
		// until it is zero
		// Fx1E add vX to I
		// Fx29 set I to the hex sprite for the lowest nibble in vX
		// Fx30 set I to the 10 lines height hex sprite for the lowest nibble in vX
		// Fx33 write the value of vX as BCD value at the addresses I, I+1 and I+2
		// Fx3A set audio pitch for a audio pattern playback rate of
		// 4000*2^((vX-64)/48)Hz
		// Fx55 write the content of v0 to vX at the memory pointed to by I, I is
		// incremented by X+1 [Quirk 11]
		// Fx65 read the bytes from memory pointed to by I into the registers v0 to vX,
		// I is incremented by X+1 [Quirk 11]
		// Fx75 store the content of the registers v0 to vX into flags storage (outside
		// of the addressable ram) [Quirk 12]
		// Fx85 load the registers v0 to vX from flags storage (outside the addressable
		// ram) [Quirk 12]
		case 0xF0:
			switch (low) {
			case 0x00:
				regI = ((memory[pc] & 0xff) << 8) + (memory[pc + 1] & 0xff);
				pc += 2;
				break;
			case 0x01:
				gpu.setPlanes(highnib);
				break;
			case 0x02:
				gpu.setAudio(regI, 16);
				break;
			case 0x07:
				vx[highnib] = regDelay;
				break;
			case 0x0A:
				gpu.updateMegaScreen();
				a = getKeyPressed();
				if (a != -1) {
					System.out.println(String.format("Key Pressed %d",a));
					vx[highnib] = a;
					while (true) {
						a = getKeyPressed();
						if (a == -1) break;
						try {
							Thread.sleep(10);
						}
						catch(Exception ex) {
							
						}
					
					}
					System.out.println("Key Released");
				}
				else
					pc -= 2;
				break;
			case 0x15:
				regDelay = vx[highnib] & 0xff;
				break;
			case 0x18:
				regSound = vx[highnib] & 0xff;
				break;
			case 0x1E:
				regI += vx[highnib] & 0xff;
				break;
			case 0x29:
				regI = spriteAdr(vx[highnib]);
				break;
			case 0x30:
				regI = sprite10Adr(vx[highnib]);
				break;
			case 0x33:
				bcd(regI, vx[highnib]);
				break;
			case 0x3a:
				gpu.setAudioPitch(vx[highnib]);
				break;
			case 0x55:
				a = regI;
				for (i = 0; i <= highnib; i++)
					memory[a++] = (byte) (vx[i] & 0xff);
				if (mNewLoad == false)
					regI = a;
				break;
			case 0x65:
				try {
				a = regI;
				for (i = 0; i <= highnib; i++)
					vx[i] = memory[a++];
				if (mNewLoad == false)
					regI = a;
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
				break;
			}
			break;

		}
	}

	private void megaPlayStop() {
		// TODO Auto-generated method stub
		
	}

	private void megaPlay(int i) {
		// TODO Auto-generated method stub
		
	}

	private int getvx(int regnr) {
		return vx[regnr] & 0xff;
	}

	private void setvx(int regnr, int value) {
		vx[regnr] = value & 0xff;

	}

	private int getKeyPressed() {
		for (int i = 0; i <= 15; i++) {
			if (keyPressed[i] == 1)
				return i;

		}
		return -1;
	}

	private int expand(int a) {
		return a & 0xff;
		// return a < 128 ? a : a-256;
	}

	private void bcd(int regI2, int number) {
		int i;
		int bp = regI2+2;
		for (i=0;i<3;i++) {
			int digit = number % 10;
			memory[bp] =(byte) digit;
			bp--;
			number /= 10;
			
		}

	}

	private int sprite10Adr(int i) {
		return 110 + 10 * i;
	}

	private int spriteAdr(int i) {
		return 10 + i * 5;
	}

	private void skip() {
		pc += 2;
		if (memory[pc] == 0xf0 && memory[pc + 1] == 0)
			pc += 2;

	}

	private void invalidOpcode() {
		// TODO Auto-generated method stub

	}

	public byte[] getMemory() {
		return memory;
	}

	public void timerTick() {
		if (regSound > 0)
			regSound--;
		if (regDelay > 0)
			regDelay--;

	}

	public void stepOver() {
		try {
			int high = memory[pc] & 0xff;
			if ((high & 0xf0) == 0x20) {
				int stop = pc + 2;
				while (pc != stop) {
					cpuTick();
				}
			} else
				cpuTick();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

	}

	public void addKey(char character) {
		int octoKey = getOctoKey(character);
		if (octoKey != -1) {
			System.out.println(String.format("Octokey %d",octoKey));
			keyPressed[octoKey] = 1;
		}
	}

	public void addRemoveKey(char character) {
		int octoKey = getOctoKey(character);
		for (int i=0;i<=15;i++) //if (octoKey != -1)
			keyPressed[i] = 0;

	}

	private int getOctoKey(char character) {
		switch (character) {
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 0xC;
		case 'q':
		case 'Q':
			return 4;
		case 'w':
		case 'W':
			return 5;
		case 'e':
		case 'E':
			return 6;
		case 'r':
		case 'R':
			return 0xD;
		case 'a':
		case 'A':
			return 7;
		case 's':
		case 'S':
			return 8;
		case 'd':
		case 'D':
			return 9;
		case 'f':
		case 'F':
			return 0xE;
		case 'y':
		case 'Y':
		case 'z':
		case 'Z':
			return 0xA;
		case 'x':
		case 'X':
			return 0;
		case 'c':
		case 'C':
			return 0xB;
		case 'v':
		case 'V':
			return 0xF;
		default:
			return -1;
		}

	}

	public boolean isBreakpoint(int pc) {
		return mBreakpoints.contains(pc);
	}

	public boolean setBreakpoint(int pc) {
/*
		int idbp = -1;
		for (int i = 0; i < 10; i++) {
			if (mBreakpoints[i] == pc) {
				idbp = i;
				break;
			}
		}
		if (idbp == -1) {
			for (int i = 0; i < 10; i++) {
				if (mBreakpoints[i] == 0) {
					mBreakpoints[i] = pc;
					countBreakpoints();
					return true;
				}
			}
		}
		mBreakpoints[idbp] = 0;
		countBreakpoints();

		return false;
		*/
		
		 boolean r = mBreakpoints.contains(pc); if (r) mBreakpoints.remove(pc); else
		 mBreakpoints.add(pc); return !r;
		 
	}
/*
	private void countBreakpoints() {
		int source = 0;
		int target = 0;
		for (int i = 0; i < 10; i++) {
			if (mBreakpoints[source] != 0) {
				if (source != target) {
					mBreakpoints[target] = mBreakpoints[source];
					target++;

					mBreakpoints[source] = 0;
				}
				source++;
			}
		}
		mNumbBreakpoints = target;

	}
*/
	public void reset() {

		for (int i = 0; i < 15; i++)
			vx[i] = 0;
		pc = 0x200;
	}

	public void setSpeed(int speed) {
		mSpeed  = speed;
		
	}

	
}
