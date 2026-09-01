package com.archi.tmpnces.presentation.common

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.mvikotlin.core.store.Store

fun <T : Store<*, *, *>> InstanceKeeperOwner.retainedStore(factory: () -> T): T =
	instanceKeeper.getOrCreate { StoreHolder(factory()) }.store

private class StoreHolder<out T : Store<*, *, *>>(
	val store: T
) : InstanceKeeper.Instance {
	
	override fun onDestroy() = store.dispose()
}