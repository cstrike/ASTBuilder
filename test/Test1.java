class Test{	
	void test{
		for (ATNState state : atn.states) {
			if (!(state instanceof StarLoopEntryState)) {
				continue;
			}

			if (((StarLoopEntryState)state).precedenceRuleDecision) {
				this.pushRecursionContextStates.set(state.stateNumber);
			}
		}

		atn.states.stream().filter((state) -> !(!(state instanceof StarLoopEntryState))).filter((state) -> (((StarLoopEntryState)state).precedenceRuleDecision)).forEach((state) -> {
			this.pushRecursionContextStates.set(state.stateNumber);
		});
	}
}