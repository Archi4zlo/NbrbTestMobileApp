package com.archi.tmpnces.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
	
	override fun migrate(connection: SQLiteConnection) {
		connection.execSQL(
			"""
			CREATE TABLE IF NOT EXISTS `rate_dynamics` (
				`curId` INTEGER NOT NULL,
				`date` TEXT NOT NULL,
				`officialRate` REAL NOT NULL,
				PRIMARY KEY(`curId`, `date`)
			)
			""".trimIndent()
				.replace("\n", "")
				.replace("\t", "")
		)
	}
}