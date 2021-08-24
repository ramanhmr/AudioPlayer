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
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            (activity as MainActivity).removeInfo()
        }
    }

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
            backPressedCallback
        )

        val metadata = requireArguments().getParcelable<MediaMetadataCompat>(METADATA_BUNDLE_KEY)!!
        with(binding!!) {
            val uri = Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
            this.ivArt.setImageBitmap(artRepository.getAlbumArt(uri, requireContext()))
            val title = metadata.getString(MetadataUtils.TITLE)
            val artist = metadata.getString(MetadataUtils.ARTIST)
            infoViewModel.getLyrics(title, artist)
            tvTitle.text = title
            tvTitle.isSelected = true
            tvAlbum.text = metadata.getString(MetadataUtils.ALBUM)
            tvAlbum.isSelected = true
            tvArtist.text = artist
            tvEndTime.text =
                MetadataUtils.durationToString(metadata.getLong(MetadataUtils.DURATION))
            tvEndTime.isSelected = true
            seekBar.max =
                metadata.getLong(MetadataUtils.DURATION).toInt() / MainViewModel.MILLIS_UPDATE
            seekBar.setOnSeekBarChangeListener(getOnSeekBarChangeListener())
            tvCurrentTime.text = MetadataUtils.durationToString(0)
            btnLyrics.setOnClickListener {
                infoViewModel.lyricsLiveData.observe(viewLifecycleOwner, {
                    (requireActivity() as MainActivity).showLyrics(it, metadata)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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

    companion object {
        private const val METADATA_BUNDLE_KEY = "METADATA_BUNDLE_KEY"

        fun newInstance(metadata: MediaMetadataCompat) = InfoFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(METADATA_BUNDLE_KEY, metadata)
            this.arguments = bundle
        }
    }
}