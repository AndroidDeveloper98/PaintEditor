package com.innovative.painteditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.*
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    ColorBottomSheetFragment.Properties,
    EditingToolsAdapter.OnItemSelected {

    private lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var mColorBottomSheetFragment: ColorBottomSheetFragment
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mRvTools: RecyclerView
    private val mEditingToolsAdapter = EditingToolsAdapter(this)
    private lateinit var mRootView: ConstraintLayout
    var selectedColor : Int = 0
    private lateinit var mSaveFileHelper: FileSaveHelper
    var mSaveImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        mColorBottomSheetFragment = ColorBottomSheetFragment()
        mColorBottomSheetFragment.setPropertiesChangeListener(this)
        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools.layoutManager = llmTools
        mRvTools.adapter = mEditingToolsAdapter
        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView).build()
        mPhotoEditor.setOnPhotoEditorListener(this)
        mPhotoEditorView.source.setImageResource(R.drawable.paris_tower)
        mPhotoEditor.setBrushDrawingMode(true)
        mShapeBuilder = ShapeBuilder()
        mPhotoEditor.setShape(mShapeBuilder)
        mSaveFileHelper = FileSaveHelper(this)
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRootView = findViewById(R.id.rootView)

        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)

        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)

        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)

        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)

        val imgShare: ImageView = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
    }


    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {

    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {

    }

    override fun onStartViewChangeListener(viewType: ViewType?) {

    }

    override fun onStopViewChangeListener(viewType: ViewType?) {

    }

    override fun onTouchSourceImage(event: MotionEvent?) {

    }

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor.undo()
            R.id.imgRedo -> mPhotoEditor.redo()
            R.id.imgSave -> saveImage()
            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
            R.id.imgShare -> shareImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_REQUEST -> try {
                    mPhotoEditor.clearAllViews()
                    val uri = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver, uri
                    )
                    mPhotoEditorView.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun shareImage() {
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(R.string.msg_save_image_to_share))
            return
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(saveImageUri))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    private fun buildFileProviderUri(uri: Uri): Uri {
        if (FileSaveHelper.isSdkHigherThan28()) {
            return uri
        }
        val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

        return FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            File(path)
        )
    }

    override fun onColorChanged(colorCode: Int,position : Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
        selectedColor = position
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            ToolType.COLORS -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mColorBottomSheetFragment.setSelectedColor(selectedColor)
                showBottomSheetDialogFragment(mColorBottomSheetFragment)
            }
            ToolType.PENCIL -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(ShapeType.Brush))
            }
            ToolType.LINE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(ShapeType.Line))
            }
            ToolType.ARROW -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(ShapeType.Arrow()))
            }
            ToolType.OVAL -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(ShapeType.Oval))
            }
            ToolType.RECTANGLE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mPhotoEditor.setShape(mShapeBuilder.withShapeType(ShapeType.Rectangle))
            }
            ToolType.ERASER -> {
                mPhotoEditor.brushEraser()
            }
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    lifecycleScope.launch {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder()
                                .setClearViewsEnabled(true)
                                .setTransparencyEnabled(true)
                                .build()

                            val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                            if (result is SaveFileResult.Success) {
                                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                hideLoading()
                                showSnackbar("Image Saved Successfully")
                                mSaveImageUri = uri
                                mPhotoEditorView.source.setImageURI(mSaveImageUri)
                            } else {
                                hideLoading()
                                showSnackbar("Failed to save Image")
                            }
                        } else {
                            hideLoading()
                            error?.let { showSnackbar(error) }
                        }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val FILE_PROVIDER_AUTHORITY = "com.innovative.painteditor.fileprovider"
        private const val PICK_REQUEST = 53
    }
}