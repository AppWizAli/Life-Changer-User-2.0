package com.enfotrix.lifechanger.ui

import android.content.Context
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.lifechanger.Adapters.WithdrawViewPagerAdapter
import com.enfotrix.lifechanger.Constants
import com.enfotrix.lifechanger.Models.InvestmentViewModel
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.Models.UserViewModel
import com.enfotrix.lifechanger.R
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.enfotrix.lifechanger.databinding.ActivityWithdrawBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityWithdraw : AppCompatActivity() {

    private lateinit var binding: ActivityWithdrawBinding
    private val investmentViewModel: InvestmentViewModel by viewModels()
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManager: SharedPrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this@ActivityWithdraw
        utils = Utils(mContext)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(mContext)
        setTitle("My Withdraws")
        binding.withdrawamount.text = totalApprovedWithdrawAmount().toString()
        getData()
        withdrawdate()
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabLayout() {
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab,
            position ->
            if (position == 0) tab.text = "Approved"
            else if (position == 1) tab.text = "Pending"
        }.attach()
    }

    private fun setupViewPager() {
        val adapter = WithdrawViewPagerAdapter(this, 2)
        binding.viewPager.adapter = adapter
    }

    private fun getData() {
        utils.startLoadingAnimation()
        lifecycleScope.launch {
            investmentViewModel.getWithdrawsReq(sharedPrefManager.getToken())
                .addOnCompleteListener { task ->
                    utils.endLoadingAnimation()
                    if (task.isSuccessful) {

                        val list = ArrayList<TransactionModel>()
                        if (task.result.size() > 0) {

                            for (document in task.result) list.add(
                                document.toObject(
                                    TransactionModel::class.java
                                )
                            )
                            sharedPrefManager.putWithdrawReqList(list)
                            setupViewPager()
                            setupTabLayout()


                        }
                    } else Toast.makeText(
                        mContext,
                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()


                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(mContext, it.message + "", Toast.LENGTH_SHORT).show()

                }

        }
    }


    override fun onBackPressed() {
        val viewPager = binding.viewPager
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }


    private fun totalApprovedWithdrawAmount(): Double {
        val approvedWithdrawTransactions = sharedPrefManager.getWithdrawReqList()
            .filter { it.status == constants.TRANSACTION_STATUS_APPROVED }
        var totalAmount = 0.0

        for (transaction in approvedWithdrawTransactions) {
            val amountString = transaction.amount
            val amount = amountString.toDoubleOrNull()
                ?: 0.0
            totalAmount += amount
        }

        return totalAmount
    }


    fun withdrawdate() {
        val approvedWithdrawTransactions = sharedPrefManager.getWithdrawReqList()
            .filter { it.status == constants.TRANSACTION_STATUS_APPROVED }
        val lastApprovedWithdrawTransaction = approvedWithdrawTransactions
            .filter { it.transactionAt != null }
            .maxByOrNull { it.transactionAt!! }
        lastApprovedWithdrawTransaction?.let {
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val formattedDate = it.transactionAt!!.toDate().let { dateFormat.format(it) }
            binding.lastwithdrawDate.text = "$formattedDate to last withdraw"
        }
    }


}