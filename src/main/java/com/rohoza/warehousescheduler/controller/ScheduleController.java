package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.service.TaskService;
import com.rohoza.warehousescheduler.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final WorkerService workerService;
    private final TaskService taskService;

    @Autowired
    public ScheduleController(WorkerService workerService, TaskService taskService) {
        this.workerService = workerService;
        this.taskService = taskService;
    }

    @GetMapping
    public String getSchedule(Model model, Principal principal) {
        Worker worker = workerService.findByUsername(principal.getName());
        List<Task> tasks = worker.getCurrentTasks()
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        model.addAttribute("workerName", worker.getFirstName() + " " + worker.getLastName());
        return "schedule";
    }

    @PostMapping("/search")
    public String searchSchedule(@RequestParam("fromDate") String fromDate,
                                 @RequestParam("toDate") String toDate, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Worker worker = workerService.findByUsername(principal.getName());

        if (fromDate.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose date range");
            return "redirect:/api/schedule";
        }

        LocalDate from;
        LocalDate to;

        if (!toDate.isEmpty()) {
            from = LocalDate.parse(fromDate);
            to = LocalDate.parse(toDate);
        }
        else {
            from = LocalDate.parse(fromDate);
            to = LocalDate.parse(fromDate);
        }

        if (from.isAfter(to)) {
            LocalDate temp = from;
            String tempDate = fromDate;
            from = to;
            fromDate = toDate;
            to = temp;
            toDate = tempDate;
        }

        List<Task> tasks = worker.findCurrentTasksByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("workerName", worker.getFirstName() + " " + worker.getLastName());
        return "schedule";
    }

    @PostMapping("/decline/{taskId}")
    public String declineTask(@PathVariable Long taskId, Principal principal) {
        Worker worker = workerService.findByUsername(principal.getName());
        Task task = taskService.findById(taskId);
        if (task != null) {
            task.declineWorker(worker);
            taskService.save(task);
        }
        return "redirect:/api/schedule";
    }
}
