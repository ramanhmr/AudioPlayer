package com.ramanhmr.audioplayer.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.ramanhmr.audioplayer.databinding.FragmentLyricsBinding
import com.ramanhmr.audioplayer.repositories.ArtRepository
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.utils.MetadataUtils
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class LyricsFragment : Fragment() {
    private var binding: FragmentLyricsBinding? = null
    private val artRepository: ArtRepository by inject()
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            (activity as MainActivity).removeLyrics()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLyricsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )

        if (arguments != null
            && requireArguments().containsKey(METADATA_BUNDLE_KEY)
            && requireArguments().containsKey(LYRICS_BUNDLE_KEY)
        ) {
            with(binding!!) {
                this.tvLyrics.text = requireArguments().getString(LYRICS_BUNDLE_KEY)
                requireArguments().getParcelable<MediaMetadataCompat>(METADATA_BUNDLE_KEY)?.let {
                    val uri = Uri.parse(it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
                    this.ivArt.setImageBitmap(artRepository.getAlbumArt(uri, requireContext()))
                    this.tvTitle.text = it.getString(MetadataUtils.TITLE)
                    this.tvTitle.isSelected = true
                    this.tvAlbum.text = it.getString(MetadataUtils.ALBUM)
                    this.tvAlbum.isSelected = true
                    this.tvArtist.text = it.getString(MetadataUtils.ARTIST)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val METADATA_BUNDLE_KEY = "METADATA_BUNDLE_KEY"
        private const val LYRICS_BUNDLE_KEY = "LYRICS_BUNDLE_KEY"

        fun newInstance(lyrics: String, metadata: MediaMetadataCompat) = LyricsFragment().apply {
            val bundle = Bundle()
            bundle.putString(LYRICS_BUNDLE_KEY, lyrics)
            bundle.putParcelable(METADATA_BUNDLE_KEY, metadata)
            this.arguments = bundle
        }
    }
}