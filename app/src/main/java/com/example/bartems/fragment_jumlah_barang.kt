package com.example.bartems

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class JumlahBarangFragment : Fragment() {

    private var listener: OnQuantityInputListener? = null
    private var productName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productName = arguments?.getString(ARG_PRODUCT_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_jumlah_barang, container, false)
        val productNameTextView: TextView = view.findViewById(R.id.text_selected_product_name)
        val inputQuantityOwn: EditText = view.findViewById(R.id.input_quantity_own)
        val inputQuantityOther: EditText = view.findViewById(R.id.input_quantity_other)
        val submitButton: Button = view.findViewById(R.id.btn_submit_quantity)

        productNameTextView.text = productName

        submitButton.setOnClickListener {
            val quantityOwn = inputQuantityOwn.text.toString().toIntOrNull() ?: 0
            val quantityOther = inputQuantityOther.text.toString().toIntOrNull() ?: 0
            listener?.onQuantityInput(quantityOwn, quantityOther)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuantityInputListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnQuantityInputListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnQuantityInputListener {
        fun onQuantityInput(quantityOwn: Int, quantityOther: Int)
    }

    companion object {
        private const val ARG_PRODUCT_NAME = "product_name"

        fun newInstance(productName: String): JumlahBarangFragment {
            val fragment = JumlahBarangFragment()
            val args = Bundle()
            args.putString(ARG_PRODUCT_NAME, productName)
            fragment.arguments = args
            return fragment
        }
    }
}
