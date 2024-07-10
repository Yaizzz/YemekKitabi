package com.yagizcandinc.yemekkitabi.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.yagizcandinc.yemekkitabi.databinding.FragmentTarifBinding


class TarifFragment : Fragment() {

    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<String> //izin istemek için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>//galeriye gitmek için
    private var secilenGorsel: Uri? = null //Uri kaynağın nerede olduğunu belirtir bize /data/media/a.jpg döner
    private var secilenBitmap: Bitmap? = null//üstteki veriyi alıp görsele çevirir

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { gorselSec(it) }
        binding.kaydetButton.setOnClickListener { kaydet(it) }
        binding.silButton.setOnClickListener { sil(it) }

        arguments?.let {
            var bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            var id = TarifFragmentArgs.fromBundle(it).id

            if (bilgi == "yeni"){
             //Yeni tarif eklenece
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.isimText.setText("")
                binding.malzemeText.setText("")

            }else{
            //Eski tarif gösteriliyor
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false
            }
        }
    }


    fun kaydet(view: View){

    }

    fun sil(view: View){

    }

    fun gorselSec(view: View){
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU){
            //izin daha önceden alındı mı alınmadı mı kontrol ettiğimiz yer
            //önceki versiyonları kendi kontrol edicek contextcompat
            //yeni izin sadece permision adı değişti              android olan manifest
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istememiz gerekiyor


                //belki kullanıcı izin vermedi yanlışlıkla veya bilerek
                //Kullanıcıya mantığını söylememiz gerekiyor mu reddetti tekrar gösterir android karar verir kaç defa olacağına veya olmayacağına
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //true dönerse snackbar göstermemiz lazım kullanıcıdan neden izin istediğimizi bir kez daha söyleyerek tekrar izin isticez
                    Snackbar.make(view,"Galeriye ulaşıp görsel seçmemiz lazım",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            }
            else//eski izin
            {
                //izin verilmiş galeriye gidebilirim
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)//StartActivityForResult başlatıyor ama geri dönüşünde gelen şeyi üstteki iflere göre ele alıyor
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemiş izin istememiz gerekiyor

                //belki kullanıcı izin vermedi yanlışlıkla veya bilerek
                //Kullanıcıya mantığını söylememiz gerekiyor mu reddetti tekrar gösterir android karar verir
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //true dönerse snackbar göstermemiz lazım kullanıcıdan neden izin istediğimizi söyleyerek tekrar izin isticez
                    Snackbar.make(view,"Galeriye ulaşıp görsel seçmemiz lazım",Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin Ver",
                        View.OnClickListener {
                            //izin isteyeceğiz
                        }
                    ).show()
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                //izin verilmiş galeriye gidebilirim
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)//StartActivityForResult başlatıyor ama geri dönüşünde gelen şeyi üstteki iflere göre ele alıyor
            }
        }
    }

    private fun registerLauncher() {

        //sanki ıntent ile acticiy paylaşır gibi ama sonucunu bize dönecek galerinin sonucunu dönecek
        //contract izin isteme de olabilir galeriye gitme de
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    //kullanıcının seçtiği görselin nerede kayıtlı olduğunu gösteriyor
                   secilenGorsel= intentFromResult.data

                    try {
                        if(Build.VERSION.SDK_INT>= 28){
                            //APi level 28 ve üstü
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }else{
                            //eski yöntem
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        //IOException input output hatası olur
                    }catch (e:Exception){
                        println(e.localizedMessage)
                    }

                }
            }
        }

        //contract (izin istemek de olur galeriye gitmek de olur) ve result callback (bunu yapıcan sonunda nolcak)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if(result){
                //izin verildi galeriye gidebiliriz -gidip bişi alıcam
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)//StartActivityForResult başlatıyor ama geri dönüşünde gelen şeyi üstteki iflere göre ele alıyor
            }else{
                //izin verilmedi kullanıcıya göster
                Toast.makeText(requireContext(),"İzin Verilmedi",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}