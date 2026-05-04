/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.view.ui.splash

import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import com.aurora.extensions.hide
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : BaseFlavouredSplashFragment() {
    override fun attachActions() {
        super.attachActions()

        binding.btnGoogle.hide()

        lifecycleScope.launch {
            delay(100)
            binding.btnAnonymous.allViews.firstOrNull{ it is MaterialButton }?.performClick()
        }
    }

    override fun resetActions() {
        super.resetActions()

        binding.btnGoogle.hide()
    }
}
