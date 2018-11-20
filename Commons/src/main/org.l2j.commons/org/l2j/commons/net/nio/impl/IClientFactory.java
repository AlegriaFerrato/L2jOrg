package org.l2j.commons.net.nio.impl;

@SuppressWarnings("rawtypes")
public interface IClientFactory<T extends MMOClient>
{
	public T create(MMOConnection<T> con);
}