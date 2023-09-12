package emulator;

public interface IEmulator {
	
	void updateScreen();

	void notifyStop();

	void log(String text);

}
