/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class SynchronousWaitListener implements IJRTraceClientListener {

	private CyclicBarrier barrier;
	private int id;
	private String msg;
	private IJRTraceVM machine;

	public SynchronousWaitListener(IJRTraceVM machine, int id, String message) {
		barrier = new CyclicBarrier(2);

		this.id = id;
		this.msg = message;
		this.machine = machine;
		machine.addClientListener(id, this);
	}

	public void waitForDone() {
		waitForDone(Integer.MAX_VALUE);
	}

	public void waitForDone(int timeout) {

		try {
			barrier.await(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException | BrokenBarrierException e) {
			// dont do anything
		} catch (TimeoutException e) {
			// dont do anything
		} finally {
			machine.removeClientListener(id, this);
		}

	}

	@Override
	public void messageReceived(String clientSentence) {
		try {
			try {
				if (clientSentence.contains(msg)) {
					barrier.await(10000, TimeUnit.SECONDS);
				}

			} catch (TimeoutException e) {
				throw new RuntimeException(e);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (BrokenBarrierException e) {
			throw new RuntimeException(e);
		}

	}
}
