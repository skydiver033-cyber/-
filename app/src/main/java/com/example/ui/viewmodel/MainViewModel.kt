package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Appointment
import com.example.data.repository.AppointmentRepository
import com.example.data.api.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Simple Data Model for E-Store Products
data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val benefits: List<String>,
    val imageUrlPlaceholder: String // Will draw custom beautiful shapes for this item
)

// Simple Data Model for Cart Item
data class CartItem(
    val product: Product,
    val quantity: Int
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppointmentRepository(db.appointmentDao())

    // 1. Navigation Tab State
    // Tabs: 0 -> Home, 1 -> Treatments Guide, 2 -> Book Appointment, 3 -> My Bookings, 4 -> Shop, 5 -> AI Advisor
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    // 2. Appointments Flow from Room DB
    val appointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun bookAppointment(
        patientName: String,
        patientPhone: String,
        therapyType: String,
        appointmentDate: String,
        appointmentTime: String,
        gender: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val appointment = Appointment(
                patientName = patientName,
                patientPhone = patientPhone,
                therapyType = therapyType,
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                gender = gender,
                notes = notes,
                status = "قيد الانتظار"
            )
            repository.insertAppointment(appointment)
            onSuccess()
        }
    }

    fun cancelAppointment(id: Int) {
        viewModelScope.launch {
            repository.updateStatus(id, "ملغى")
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // 3. Store and Cart State
    // Preset mock high quality natural healing products
    val productsList = listOf(
        Product(
            id = 1,
            name = "عسل السدر اليمني الأصلي",
            category = "عسل طبيعي",
            price = 4500.0,
            description = "عسل سدر طبيعي ممتاز 100%، يوصى به بعد جلسات الحجامة لتقوية الجهاز المناعي وتسريع شفاء وتجديد الخلايا.",
            benefits = listOf("مضاد حيوي ومقوي طبيعي للمناعة", "يساعد في التئام وشفاء مواضع كؤوس الحجامة", "يمد الجسم بالطاقة والنشاط"),
            imageUrlPlaceholder = "honey_sdr"
        ),
        Product(
            id = 2,
            name = "زيت الحبة السوداء المعصور على البارد",
            category = "زيوت علاجية",
            price = 1200.0,
            description = "زيت حبة البركة النقي والمركز والمعصور على العصارة الميكانيكية الباردة بدون حرارة للحفاظ على فاعليته القصوى.",
            benefits = listOf("مضاد للالتهابات ومخفف لآلام الظهر والمفاصل", "مرطب ويطهر مواضع الحجامة والخدوش", "يحفز الدورة الدموية للجسم"),
            imageUrlPlaceholder = "black_seed_oil"
        ),
        Product(
            id = 3,
            name = "حقيبة كؤوس الحجامة المعقمة",
            category = "أدوات ومعدات",
            price = 2500.0,
            description = "مجموعة متكاملة من كؤوس ومضخة الحجامة للاستخدام الفردي الخاص لضمان أعلى مستويات الأمان والتعقيم التام لمنع العدوى.",
            benefits = listOf("مغلفة بإحكام ومعقمة تحت الرقابة الطبية", "تحوي كؤوس بمقاسات مختلفة لتناسب جميع مناطق الجسم", "مصنوعة من بلاستيك طبي مقاوم للكسر والضغط"),
            imageUrlPlaceholder = "cupping_set"
        ),
        Product(
            id = 4,
            name = "مسحوق القسط الهندي الأصلي",
            category = "أعشاب ونباتات",
            price = 1800.0,
            description = "قسط هندي ممتاز بجودة نقية، مطحون بدقة ومستورد خصيصاً، يعد من أبرز الأدوية النبوية مع الحجامة.",
            benefits = listOf("يساعد في تنظيم الغدد الهرمونية وخاصة الدرقية", "مقوي للجهاز التنفسي وعلاج حساسية الصدر", "مكافح ممتاز للسموم والالتهابات الداخلية"),
            imageUrlPlaceholder = "costus_powder"
        ),
        Product(
            id = 5,
            name = "زيت زيتون بكر طبيعي مرقى عالي الجودة",
            category = "زيوت طبيعية",
            price = 950.0,
            description = "زيت زيتون جزائري بكر ممتاز مستخلص من العصرة الأولى، مضاف إليه قراءات وتلاوات للرقية الشرعية لشفاء الروح والجسد.",
            benefits = listOf("مبارك ومفيد للدهن الموضعي بعد الحجامة", "مغذٍ غني بالأوميغا وطارد للعين والتابعة والسموم", "يزيد من مرونة الأنسجة ونعومة الجلد"),
            imageUrlPlaceholder = "olive_oil"
        ),
        Product(
            id = 6,
            name = "خلطة العسل الملكية لزيادة الخصوبة والنشاط",
            category = "مكملات مغذية",
            price = 6000.0,
            description = "تركيبة علاجية فريدة تجمع بين عسل دوعني دافئ وغذاء ملكات النحل المركز مع حبوب اللقاح وغبار البروبوليس المعقم.",
            benefits = listOf("تنشيط القوة الذهنية والبدنية وعلاج الخمور والكسل", "تحفيز إفراز الهرمونات وزيادة طاقة الخصوبة للرجل والمرأة", "غني بالأحماض الأمينية والمعادن الحيوية لإعادة ترميم الخلايا"),
            imageUrlPlaceholder = "royal_honey_mix"
        )
    )

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun addToCart(product: Product) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(quantity = currentList[index].quantity + 1)
        } else {
            currentList.add(CartItem(product, 1))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Int, change: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val newQty = currentList[index].quantity + change
            if (newQty > 0) {
                currentList[index] = currentList[index].copy(quantity = newQty)
            } else {
                currentList.removeAt(index)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Int) {
        val currentList = _cart.value.filter { it.product.id != productId }
        _cart.value = currentList
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.product.price * it.quantity }
    }

    // 4. AI Health Advisor Chat State
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Setup an initial welcome message from the AI specialist
    init {
        _chatHistory.value = listOf(
            "" to "أهلاً بك في العيادة الطبية لمسعود للحجامة! 🌸 أنا مستشارك الصحي الذكي المساعد هنا للإجابة على جميع تساؤلاتك حول الحجامة الطبية كالعلاجية والوقائية والرياضية، وفوائد الإبر الصينية وسم النحل والتحضيرات اللازمة وأي استشارات عامة. كيف يمكنني مساعدتك ودعم صحتك اليوم؟"
        )
    }

    fun sendMessageToAI(message: String) {
        if (message.isBlank()) return
        
        val currentHistory = _chatHistory.value.toMutableList()
        _chatHistory.value = currentHistory + (message to "")
        _isChatLoading.value = true

        viewModelScope.launch {
            // Prepare history structure for the API (filter out the initial helper greeting if we want, or supply it as context)
            val apiHistory = _chatHistory.value
                .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }

            val response = GeminiClient.chatWithAdvisor(message, apiHistory)
            
            // Update the history with response
            val updatedHistory = _chatHistory.value.toMutableList()
            if (updatedHistory.isNotEmpty() && updatedHistory.last().first == message) {
                updatedHistory[updatedHistory.lastIndex] = message to response
            } else {
                updatedHistory.add(message to response)
            }
            _chatHistory.value = updatedHistory
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            "" to "أهلاً بك مجدداً في العيادة الطبية لمسعود للحجامة! 🌸 أنا مستشارك الصحي المساعد ومستعد لأي سؤال جديد حول خدماتنا الوقائية أو العلاجية. كيف أستطيع خدمتك الآن؟"
        )
    }
}
