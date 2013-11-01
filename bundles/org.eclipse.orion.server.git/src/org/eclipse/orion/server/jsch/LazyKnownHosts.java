/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.jsch;

import com.jcraft.jsch.*;

/**
 * Use this repository instead of standard {@link HostKeyRepository} to record the last checked host fingerprint.
 * If the last status recorded as {@link #lastStatus}, if check in the repository is different than {@link HostKeyRepository#OK}
 * the host and its key are recorded as {@link #lastUnknownHost} and {@link #lastUnknownKey}.
 *
 */
public class LazyKnownHosts implements HostKeyRepository {

	private HostKeyRepository repo;

	private String lastUnknownHost = null;
	private byte[] lastUnknownKey = null;
	private int lastStatus = OK;

	LazyKnownHosts(JSch jsch, String knownHosts) throws JSchException {
		if (knownHosts != null) {
			jsch.setKnownHosts(knownHosts);
		}
		this.repo = jsch.getHostKeyRepository();

	}

	@Override
	public int check(String host, byte[] key) {
		lastStatus = repo.check(host, key);

		if (lastStatus != OK) {
			lastUnknownHost = host;
			lastUnknownKey = key;
		} else {
			lastUnknownHost = null;
			lastUnknownKey = null;
		}
		return lastStatus;
	}

	@Override
	public void add(HostKey hostkey, UserInfo ui) {
		repo.add(hostkey, ui);
	}

	@Override
	public void remove(String host, String type) {
		repo.remove(host, type);
	}

	@Override
	public void remove(String host, String type, byte[] key) {
		repo.remove(host, type, key);
	}

	@Override
	public String getKnownHostsRepositoryID() {
		return "LAZY_" + repo.getKnownHostsRepositoryID();
	}

	@Override
	public HostKey[] getHostKey() {
		return repo.getHostKey();
	}

	@Override
	public HostKey[] getHostKey(String host, String type) {
		return repo.getHostKey();
	}

	public String getLastUnknownkedHost() {
		return lastUnknownHost;
	}

	public byte[] getLastUnknownKey() {
		return lastUnknownKey;
	}

	public int getLastStatus() {
		return lastStatus;
	}

}
