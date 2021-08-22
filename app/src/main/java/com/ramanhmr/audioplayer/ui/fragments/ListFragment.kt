package com.ramanhmr.audioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.databinding.FragmentListBinding
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.ui.TrackAdapter
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ListFragment : Fragment() {
    private var binding: FragmentListBinding? = null
    private val mainViewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.divider, null)!!)
        val trackAdapter = TrackAdapter(activity as MainActivity)
        binding!!.rvSongs.apply {
            adapter = trackAdapter
            addItemDecoration(divider)
        }

        mainViewModel.audioLiveData.observe(
            viewLifecycleOwner,
            { trackAdapter.submitList(it) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}