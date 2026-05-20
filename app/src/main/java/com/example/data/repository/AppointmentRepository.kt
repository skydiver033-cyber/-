package com.example.data.repository

import com.example.data.database.AppointmentDao
import com.example.data.model.Appointment
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val appointmentDao: AppointmentDao) {
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()

    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment)
    }

    suspend fun updateStatus(id: Int, status: String) {
        appointmentDao.updateStatus(id, status)
    }

    suspend fun deleteById(id: Int) {
        appointmentDao.deleteAppointmentById(id)
    }
}
