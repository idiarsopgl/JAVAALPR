package com.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Mock data for dashboard
        model.addAttribute("availableSlots", 45);
        model.addAttribute("occupiedSlots", 15);
        model.addAttribute("todayRevenue", "Rp 1.250.000");
        model.addAttribute("activeOperators", 3);
        
        // Mock data for recent activities
        model.addAttribute("recentActivities", generateMockActivities());
        
        return "dashboard";
    }
    
    @GetMapping("/slots")
    public String parkingSlots(Model model) {
        return "slots";
    }
    
    @GetMapping("/operators")
    public String operators(Model model) {
        return "operators";
    }
    
    @GetMapping("/shifts")
    public String shifts(Model model) {
        return "shifts";
    }
    
    @GetMapping("/rates")
    public String rates(Model model) {
        return "rates";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        return "reports";
    }
    
    @GetMapping("/analytics")
    public String analytics(Model model) {
        return "analytics";
    }
    
    @GetMapping("/vehicle-entry")
    public String vehicleEntry(Model model) {
        return "vehicle-entry";
    }
    
    @GetMapping("/vehicle-exit")
    public String vehicleExit(Model model) {
        return "vehicle-exit";
    }
    
    private Object generateMockActivities() {
        // This would be replaced with actual data from a service
        return java.util.Arrays.asList(
            new Activity("2024-03-15 08:30:45", "B 1234 XYZ", "Entry", "Operator 1"),
            new Activity("2024-03-15 09:15:22", "D 5678 ABC", "Entry", "Operator 2"),
            new Activity("2024-03-15 10:05:11", "F 9012 DEF", "Exit", "Operator 1"),
            new Activity("2024-03-15 10:45:33", "B 3456 GHI", "Entry", "Operator 3"),
            new Activity("2024-03-15 11:20:17", "D 7890 JKL", "Exit", "Operator 2")
        );
    }
    
    // Inner class for activity data
    private static class Activity {
        private String time;
        private String vehicle;
        private String action;
        private String operator;
        
        public Activity(String time, String vehicle, String action, String operator) {
            this.time = time;
            this.vehicle = vehicle;
            this.action = action;
            this.operator = operator;
        }
        
        public String getTime() { return time; }
        public String getVehicle() { return vehicle; }
        public String getAction() { return action; }
        public String getOperator() { return operator; }
    }
}