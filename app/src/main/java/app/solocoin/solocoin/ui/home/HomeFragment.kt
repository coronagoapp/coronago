package app.solocoin.solocoin.ui.home

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.solocoin.solocoin.R
import app.solocoin.solocoin.app.SolocoinApp.Companion.sharedPrefs
import app.solocoin.solocoin.util.AppDialog
import app.solocoin.solocoin.util.GlobalUtils
import app.solocoin.solocoin.util.enums.Status
import com.anupkumarpanwar.scratchview.ScratchView
import com.anupkumarpanwar.scratchview.ScratchView.IRevealListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel


@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.M)
class HomeFragment : Fragment() {

    private val TAG = HomeFragment::class.simpleName

    private val viewModel: HomeFragmentViewModel by viewModel()

    private var tvHomeDuration: TextView? = null
    private lateinit var context: Activity
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = requireActivity()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvHomeDuration = view.findViewById(R.id.time)
        sharedPrefs?.visited?.let {
            if (it[0]) {
                sharedPrefs?.visited = arrayListOf(false, it[1], it[2])
                val infoDialog = AppDialog.instance(
                    "",
                    getString(R.string.new_user_intro),
                    object : AppDialog.AppDialogListener {
                        override fun onClickConfirm() {
                            showIntro()
                        }
                        override fun onClickCancel() {
                            showIntro()
                        }
                    })
                infoDialog.show(requireFragmentManager(), infoDialog.tag)
            }
        }
        scratch_card_image.setOnClickListener {
            showDialog()
        }
        updateTime()

        quiz_viewpager.adapter = QuizFragmentAdapter(this)

        TabLayoutMediator(quiz_tablayout, quiz_viewpager) { tab, position ->
            tab.text = tabHeading[position]
        }.attach()
    }

    fun showDialog(){
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.scratch_card)
        val scratchView: ScratchView = dialog.findViewById(R.id.scratch_view)

        scratchView.setRevealListener(object :IRevealListener{
            override fun onRevealed(scratchView: ScratchView?) {
                Toast.makeText(context,"Congratulations!!",Toast.LENGTH_LONG).show()
                scratchView?.visibility=View.GONE
                scratch_card_image.visibility=View.GONE
            }

            override fun onRevealPercentChangedListener(scratchView: ScratchView?, percent: Float) {
                if(percent>0.5){
//                    Toast.makeText(context, "Revealed!$percent",Toast.LENGTH_LONG).show()
                }
            }
        })
        dialog.show()

    }
    private fun updateTime() {
        viewModel.userData().observe(viewLifecycleOwner, Observer { response ->
//            Log.d(TAG + "After Login/SignUp", "$response")
            when (response.status) {
                Status.SUCCESS -> {
                    val duration =
                        GlobalUtils.parseJsonNullFieldValue(response.data?.get("home_duration_in_seconds"))?.asLong
                    if (duration != 0L && duration != null) {
                        tvHomeDuration?.text = GlobalUtils.formattedHomeDuration(duration)
                        sharedPrefs?.homeDuration = duration
                    }
                }
                Status.ERROR -> {
                    if (sharedPrefs?.homeDuration != 0L) {
                        tvHomeDuration?.text = GlobalUtils.formattedHomeDuration(sharedPrefs?.homeDuration)
                    }
                }
                Status.LOADING -> {}
            }
        })
    }

    private fun showIntro() {
        with(requireActivity()) {
            val intro = findViewById<ImageView>(R.id.intro).apply {
                setImageResource(R.drawable.intro_home)
                visibility = View.VISIBLE
            }
            findViewById<ImageButton>(R.id.close_bt).apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    intro.visibility = View.GONE
                    it.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        fun instance() = HomeFragment().apply {}
        private const val TAB_COUNT = 2
        private val tabHeading = arrayOf("DAILY", "WEEKLY")
    }

    private class QuizFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return TAB_COUNT
        }

        override fun createFragment(position: Int): Fragment {
            return QuizFragment(position)
        }
    }
}
