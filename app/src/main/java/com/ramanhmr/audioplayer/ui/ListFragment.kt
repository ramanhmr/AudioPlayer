package com.ramanhmr.audioplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ramanhmr.audioplayer.databinding.FragmentListBinding
import com.ramanhmr.audioplayer.viewmodels.MainViewModel

class ListFragment : Fragment() {
    private var binding: FragmentListBinding? = null
    private val mainViewModel: MainViewModel by activityViewModels()

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

        val adapter = TrackAdapter(activity as MainActivity)
        binding?.rvSongs?.adapter = adapter
        mainViewModel.audioLiveData.observe(viewLifecycleOwner, Observer { adapter.submitList(it) })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}