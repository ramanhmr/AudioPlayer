package com.ramanhmr.audioplayer.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.ramanhmr.audioplayer.databinding.FragmentInfoBinding
import com.ramanhmr.audioplayer.repositories.ArtRepository
import com.ramanhmr.audioplayer.services.PlayerService
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.utils.MetadataUtils
import com.ramanhmr.audioplayer.viewmodels.InfoViewModel
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class InfoFragment : Fragment() {
    private var binding: FragmentInfoBinding? = null
    private val mainViewModel: MainViewModel by sharedViewModel()
    private val infoViewModel: InfoViewModel by viewModel()
    private val artRepository: ArtRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as MainActivity).removeInfo()
                }
            })

        if (arguments != null && requireArguments().containsKey(METADATA_BUNDLE_KEY)) {
            with(requireArguments().getParcelable<MediaMetadataCompat>(METADATA_BUNDLE_KEY)!!) {
                binding?.let {
                    val uri = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
                    it.ivArt.setImageBitmap(artRepository.getAlbumArt(uri, requireContext()))
                    val title = this.getString(MetadataUtils.TITLE)
                    val artist = this.getString(MetadataUtils.ARTIST)
                    infoViewModel.getLyrics(title, artist)
                    it.tvTitle.text = title
                    it.tvTitle.isSelected = true
                    it.tvAlbum.text = this.getString(MetadataUtils.ALBUM)
                    it.tvAlbum.isSelected = true
                    it.tvArtist.text = artist
                    it.tvEndTime.text =
                        MetadataUtils.durationToString(this.getLong(MetadataUtils.DURATION))
                    it.tvEndTime.isSelected = true
                    it.seekBar.max =
                        this.getLong(MetadataUtils.DURATION).toInt() / MainViewModel.MILLIS_UPDATE
                    it.seekBar.setOnSeekBarChangeListener(getOnSeekBarChangeListener())
                    it.tvCurrentTime.text = MetadataUtils.durationToString(0)
                }
            }
        }
        with(binding!!) {
            btnLyrics.setOnClickListener {
                infoViewModel.lyricsLiveData.observe(viewLifecycleOwner, {
                    tvLyrics.visibility = View.VISIBLE
                    tvLyrics.text = it
                })

            }
        }
        requireActivity().mediaController.transportControls.sendCustomAction(
            PlayerService.REQUEST_STATE,
            null
        )
        mainViewModel.positionLiveData.observe(viewLifecycleOwner, {
            updateProgress(it / MainViewModel.MILLIS_UPDATE)
            binding!!.tvCurrentTime.text = MetadataUtils.durationToString(it)
        })
    }

    private fun getOnSeekBarChangeListener() = object : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                activity!!.mediaController.transportControls.seekTo(progress * MainViewModel.MILLIS_UPDATE.toLong())
                mainViewModel.setProgress(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }

    private fun updateProgress(progress: Int) {
        binding!!.seekBar.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val METADATA_BUNDLE_KEY = "METADATA_BUNDLE_KEY"

        fun newInstance(metadata: MediaMetadataCompat) = InfoFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(METADATA_BUNDLE_KEY, metadata)
            this.arguments = bundle
        }
    }
}