package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Location;
import com.rohoza.warehousescheduler.model.Task;
import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.scheduler.Scheduler;
import com.rohoza.warehousescheduler.service.LocationService;
import com.rohoza.warehousescheduler.service.TaskService;
import com.rohoza.warehousescheduler.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final LocationService locationService;
    private final WorkerService workerService;

    @Autowired
    public TaskController(TaskService taskService, LocationService locationService, WorkerService workerService) {
        this.taskService = taskService;
        this.locationService = locationService;
        this.workerService = workerService;
    }

    @GetMapping
    public String getAllCurrentTasks(Model model) {
        List<Task> tasks = taskService.findCurrentAll()
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());

        long assigned = tasks.stream().filter(Task::isAssigned).count();
        if (assigned == tasks.size()) {
            model.addAttribute("successMessage", "All tasks are assigned");
        } else {
            model.addAttribute("resultMessage", assigned + " tasks out of " + tasks.size() + " are assigned");
        }

        model.addAttribute("tasks", tasks);
        return "task-list";
    }

    @PostMapping("/search")
    public String searchCurrentTasks(@RequestParam("fromDate") String fromDate,
                                     @RequestParam("toDate") String toDate, Model model,
                                     RedirectAttributes redirectAttributes) {
        if (fromDate.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose date range");
            return "redirect:/api/tasks";
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
            redirectAttributes.addFlashAttribute("errorMessage", "Start date is after finish date");
            return "redirect:/api/tasks";
        }

        List<Task> tasks = taskService.findCurrentByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());

        long assigned = tasks.stream().filter(Task::isAssigned).count();
        if (assigned == tasks.size()) {
            model.addAttribute("successMessage", "All tasks are assigned");
        } else {
            model.addAttribute("resultMessage", assigned + " tasks out of " + tasks.size() + " are assigned");
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "task-list";
    }

    @GetMapping("/archive")
    public String getAllArchiveTasks(Model model) {
        List<Task> tasks = taskService.findArchiveAll()
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime).reversed())
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        return "task-list-archive";
    }

    @PostMapping("/archive/search")
    public String searchArchiveTasks(@RequestParam("fromDate") String fromDate,
                                     @RequestParam("toDate") String toDate, Model model,
                                     RedirectAttributes redirectAttributes) {
        if (fromDate.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose date range");
            return "redirect:/api/tasks/archive";
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
            redirectAttributes.addFlashAttribute("errorMessage", "Start date is after finish date");
            return "redirect:/api/tasks/archive";
        }

        List<Task> tasks = taskService.findArchiveByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime).reversed())
                .collect(Collectors.toList());
        model.addAttribute("tasks", tasks);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "task-list-archive";
    }

    @PostMapping("/generate-schedule")
    public String generateSchedule(@RequestParam("fromDate") String fromDate,
                                   @RequestParam("toDate") String toDate, Model model,
                                   RedirectAttributes redirectAttributes) {
        if (fromDate.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Choose date range");
            return "redirect:/api/tasks";
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
            redirectAttributes.addFlashAttribute("errorMessage", "Start date is after finish date");
            return "redirect:/api/tasks";
        }

        List<Task> tasks = taskService.findCurrentByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59))
                .stream()
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
        List<Worker> allWorkers = workerService.findAll();
        Scheduler scheduler = new Scheduler(tasks, allWorkers);
        int assigned = scheduler.generate();
        for (Task task : tasks) {
            taskService.save(task);
        }
        if (assigned == tasks.size()) {
            model.addAttribute("successMessage", "Assigned all tasks");
        } else {
            model.addAttribute("resultMessage", "Assigned " + assigned + " tasks out of " + tasks.size());
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "task-list";
    }

    @GetMapping("/add-task")
    public String showAddTaskForm(Model model) {
        List<Location> allLocations = locationService.findAll();
        model.addAttribute("allLocations", allLocations);
        model.addAttribute("task", new Task());
        return "add-task";
    }

    @PostMapping("/add-task")
    public String createTask(@RequestParam("description") String description,
                             @RequestParam("startTime") String startTime,
                             @RequestParam("finishTime") String finishTime,
                             @RequestParam("neededWorkers") int neededWorkers,
                             @RequestParam("locationId") Long locationId,
                             RedirectAttributes redirectAttributes) {
        LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime finish = LocalDateTime.parse(finishTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (start.isAfter(finish)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Start time is after finish time");
            return "redirect:/api/tasks/add-task";
        }
        Location location = locationService.findById(locationId);

        Task task = new Task(description, start, finish, neededWorkers, location);
        taskService.save(task);

        return "redirect:/api/tasks";
    }

    @GetMapping("/manage-task/{id}")
    public String manageTask(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        List<Worker> availableWorkers = workerService.findAvailableWorkersForTask(task);
        List<Worker> assignedWorkers = task.getAssignedWorkers();

        // Add assigned workers to available workers list
        for (Worker worker : assignedWorkers) {
            if (!availableWorkers.contains(worker)) {
                availableWorkers.add(worker);
            }
        }

        availableWorkers.sort(Comparator.comparing(Worker::getFirstName)
                .thenComparing(Worker::getLastName));

        // Get assigned workers' names
        String assignedWorkersLine = task.getAssignedWorkers().stream()
                .map(worker -> worker.getFirstName() + " " + worker.getLastName())
                .reduce((w1, w2) -> w1 + ", " + w2)
                .orElse("No assigned workers");

        model.addAttribute("task", task);
        model.addAttribute("availableWorkers", availableWorkers);
        model.addAttribute("assignedWorkersLine", assignedWorkersLine);
        return "manage-task";
    }

    @PostMapping("/assign-task/{id}")
    public String assignTask(@PathVariable Long id,
                             @RequestParam(value = "workerIds", required = false, defaultValue = "") List<Long> workerIds,
                             RedirectAttributes redirectAttributes) {
        Task task = taskService.findById(id);
        List<Worker> workers = workerService.findAllById(workerIds);
        if (task.getNeededWorkers() > workers.size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Not enough workers");
            return "redirect:/api/tasks/manage-task/" + id;
        }
        if (task.getNeededWorkers() < workers.size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Too many workers");
            return "redirect:/api/tasks/manage-task/" + id;
        }
        task.assignManually(workers);
        taskService.save(task);
        return "redirect:/api/tasks";
    }

    @GetMapping("/unassign-task/{id}")
    public String unassignTask(@PathVariable Long id) {
        Task task = taskService.findById(id);
        task.unAssign();
        taskService.save(task);
        return "redirect:/api/tasks/manage-task/" + id;
    }

    @GetMapping("/edit-task/{id}")
    public String showEditTaskForm(@PathVariable("id") Long id, Model model) {
        Task task = taskService.findById(id);
        List<Location> allLocations = locationService.findAll();

        model.addAttribute("task", task);
        model.addAttribute("allLocations", allLocations);

        return "edit-task";
    }

    @PostMapping("/edit-task/{id}")
    public String updateTask(@PathVariable Long id,
                             @RequestParam("description") String description,
                             @RequestParam("startTime") String startTime,
                             @RequestParam("finishTime") String finishTime,
                             @RequestParam("neededWorkers") int neededWorkers,
                             @RequestParam("locationId") Long locationId,
                             RedirectAttributes redirectAttributes) {
        LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime finish = LocalDateTime.parse(finishTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (start.isAfter(finish)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Start time is after finish time");
            return "redirect:/api/tasks/edit-task/" + id;
        }
        Task task = taskService.findById(id);
        task.setDescription(description);
        task.setStartTime(start);
        task.setFinishTime(finish);
        task.setNeededWorkers(neededWorkers);
        task.setLocation(locationService.findById(locationId));
        taskService.save(task);

        return "redirect:/api/tasks";
    }

    @GetMapping("/show-task/{id}")
    public String showTask(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        if (task != null) {
            model.addAttribute("task", task);

            // Get assigned workers' names
            String assignedWorkers = task.getAssignedWorkers().stream()
                    .map(worker -> worker.getFirstName() + " " + worker.getLastName())
                    .reduce((w1, w2) -> w1 + ", " + w2)
                    .orElse("No assigned workers");

            // Get declined workers' names
            String declinedWorkers = task.getDeclinedWorkers().stream()
                    .map(worker -> worker.getFirstName() + " " + worker.getLastName())
                    .reduce((w1, w2) -> w1 + ", " + w2)
                    .orElse("No declined workers");

            model.addAttribute("assignedWorkers", assignedWorkers);
            model.addAttribute("declinedWorkers", declinedWorkers);

            return "show-task";
        } else {
            return "redirect:/api/tasks";
        }
    }

    @GetMapping("/archive/show-task/{id}")
    public String showTaskArchive(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        if (task != null) {
            model.addAttribute("task", task);

            // Get assigned workers' names
            String assignedWorkers = task.getAssignedWorkers().stream()
                    .map(worker -> worker.getFirstName() + " " + worker.getLastName())
                    .reduce((w1, w2) -> w1 + ", " + w2)
                    .orElse("No assigned workers");

            model.addAttribute("assignedWorkers", assignedWorkers);

            return "show-task-archive";
        } else {
            return "redirect:/api/tasks/archive";
        }
    }

    @GetMapping("/delete-task/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteById(id);
        return "redirect:/api/tasks";
    }

    @PostMapping("/{type}/report")
    public String searchTasksAsText(@PathVariable String type,
                                    @RequestParam("fromDate") String fromDate,
                                    @RequestParam("toDate") String toDate, Model model,
                                    RedirectAttributes redirectAttributes) {
        List<Task> tasks;
        if (fromDate.isEmpty()) {
            if (type.equals("archive")) {
                tasks = taskService.findArchiveAll();
            }
            else {
                tasks = taskService.findCurrentAll();

            }
            tasks = tasks.stream().sorted(Comparator.comparing(Task::getStartTime))
                    .collect(Collectors.toList());
        } else {
            if (toDate.isEmpty()) {
                toDate = fromDate;
            }

            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);

            if (from.isAfter(to)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Start date is after finish date");
                if (type.equals("archive"))
                    return "redirect:/api/tasks/archive";
                else
                    return "redirect:/api/tasks";
            }

            if (type.equals("archive")) {
                tasks = taskService.findArchiveByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59));
            } else {
                tasks = taskService.findCurrentByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59));
            }
            tasks = tasks.stream()
                    .sorted(Comparator.comparing(Task::getStartTime))
                    .collect(Collectors.toList());
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "task-report";
    }
}
