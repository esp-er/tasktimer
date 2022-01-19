package patriker.tasktimer;

interface ClockTickListener{

  void Tick(int secondsRemaining);

	void finalTick();
}
