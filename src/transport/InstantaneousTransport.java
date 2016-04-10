package transport;

import peersim.config.Configuration;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.Transport;

/**
 * Transport class to send between nodes.
 * 
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 * 
 */
public final class InstantaneousTransport implements Transport {
	private final long CONSTANT_LATENCY;

	// ---------------------------------------------------------------------
	// Initialization
	// ---------------------------------------------------------------------

	/**
	 * Reads configuration parameter.
	 */
	public InstantaneousTransport(String prefix) {
		CONSTANT_LATENCY = Configuration.getLong(prefix+"."+"latency");
	}


	/**
	 * Delivers the message with a random delay, that is drawn from the
	 * configured interval according to the uniform distribution.
	 */
	public void send(Node src, Node dest, Object msg, int pid) {
		EDSimulator.add(CONSTANT_LATENCY, msg, dest, pid);
	}

	public long getLatency(Node src, Node dest) {
		return CONSTANT_LATENCY;
	}
	
	public Object clone() {
		return this;
	}
}
