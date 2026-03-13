package com.specialistbooking.serviceImpl;

import com.specialistbooking.dto.request.BulkScheduleRequest;
import com.specialistbooking.dto.request.ScheduleRequest;
import com.specialistbooking.entity.Doctor;
import com.specialistbooking.entity.Schedule;
import com.specialistbooking.exception.ResourceNotFoundException;
import com.specialistbooking.repository.DoctorRepository;
import com.specialistbooking.repository.ScheduleRepository;
import com.specialistbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Schedule createSchedule(ScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setDate(request.getDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setBooked(false);
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAvailableSlots(Long doctorId) {
        return scheduleRepository.findByDoctor_IdAndIsBookedFalse(doctorId);
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctor_Id(doctorId);
    }

    @Override
    public Schedule updateSchedule(Long id, ScheduleRequest request) {
        Schedule schedule = getScheduleById(id);
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        
        schedule.setDoctor(doctor);
        schedule.setDate(request.getDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        return scheduleRepository.save(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        Schedule schedule = getScheduleById(id);
        scheduleRepository.delete(schedule);
    }

    @Override
    public List<Schedule> createBulkSchedules(List<ScheduleRequest> requests) {
        return requests.stream()
            .map(this::createSchedule)
            .toList();
    }

    @Override
    public List<Schedule> createBulkSchedulesFromPattern(BulkScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<Schedule> schedules = new ArrayList<>();
        int slotDuration = request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30;

        if (request.getRecurringPattern() != null) {
            BulkScheduleRequest.RecurringPattern pattern = request.getRecurringPattern();
            LocalDate fromDate = pattern.getFromDate();
            LocalDate toDate = pattern.getToDate();
            LocalTime startTime = pattern.getStartTime();
            LocalTime endTime = pattern.getEndTime();
            Set<DayOfWeek> daysOfWeek = pattern.getDaysOfWeek();

            // Iterate through each day in the date range
            LocalDate currentDate = fromDate;
            while (!currentDate.isAfter(toDate)) {
                // Check if this day of week should have slots
                if (daysOfWeek == null || daysOfWeek.isEmpty() || daysOfWeek.contains(currentDate.getDayOfWeek())) {
                    // Generate slots for this day
                    LocalTime slotStart = startTime;
                    while (slotStart.plusMinutes(slotDuration).compareTo(endTime) <= 0) {
                        LocalTime slotEnd = slotStart.plusMinutes(slotDuration);
                        
                        Schedule schedule = new Schedule();
                        schedule.setDoctor(doctor);
                        schedule.setDate(currentDate);
                        schedule.setStartTime(slotStart);
                        schedule.setEndTime(slotEnd);
                        schedule.setBooked(false);
                        
                        schedules.add(scheduleRepository.save(schedule));
                        slotStart = slotEnd;
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        // Handle manual schedules if provided
        if (request.getManualSchedules() != null) {
            for (BulkScheduleRequest.ManualSchedule manual : request.getManualSchedules()) {
                Schedule schedule = new Schedule();
                schedule.setDoctor(doctor);
                schedule.setDate(manual.getDate());
                schedule.setStartTime(manual.getStartTime());
                schedule.setEndTime(manual.getEndTime());
                schedule.setBooked(false);
                schedules.add(scheduleRepository.save(schedule));
            }
        }

        return schedules;
    }
}