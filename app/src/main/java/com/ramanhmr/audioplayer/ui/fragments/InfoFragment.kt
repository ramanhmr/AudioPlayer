package com.ramanhmr.audioplayer.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.ramanhmr.audioplayer.databinding.FragmentInfoBinding
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.utils.MetadataUtils
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class InfoFragment : Fragment() {
    private var binding: FragmentInfoBinding? = null

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
                    it.ivArt.setImageBitmap(MetadataUtils.getAlbumArt(uri, requireContext()))
                    it.tvTitle.text = this.getString(MetadataUtils.TITLE)
                    it.tvTitle.isSelected = true
                    it.tvAlbum.text = this.getString(MetadataUtils.ALBUM)
                    it.tvAlbum.isSelected = true
                    it.tvArtist.text = this.getString(MetadataUtils.ARTIST)
                    it.tvEndTime.text =
                        MetadataUtils.durationToString(this.getLong(MetadataUtils.DURATION))
                    it.tvEndTime.isSelected = true
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

        fun newInstance(metadata: MediaMetadataCompat) = InfoFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(METADATA_BUNDLE_KEY, metadata)
            this.arguments = bundle
        }
    }
}