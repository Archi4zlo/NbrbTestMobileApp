package com.archi.tmpnces.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.archi.tmpnces.data.local.migration.MIGRATION_1_2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
	
	@get:Rule val helper = MigrationTestHelper(
		InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java
	)
	
	@Test
	fun migrate1To2_createsDynamicsTable() {
		helper.createDatabase(TEST_DB, 1)
			.apply {
				execSQL(
					"""
				INSERT INTO rates (date, curId, abbreviation, name, scale, officialRate)
				VALUES ('2026-09-02', 431, 'USD', 'Доллар США', 1, 3.0629)
				"""
				)
				close()
			}
		
		val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
		
		db.query("SELECT COUNT(*) FROM rate_dynamics")
			.use { cursor ->
				assertTrue(cursor.moveToFirst())
				assertEquals(0, cursor.getInt(0))
			}
		
		db.query("SELECT officialRate FROM rates WHERE curId = 431")
			.use { cursor ->
				assertTrue(cursor.moveToFirst())
				assertEquals(3.0629, cursor.getDouble(0), 1e-9)
			}
		
		db.close()
	}
	
	private companion object {
		const val TEST_DB = "migration-test.db"
	}
}