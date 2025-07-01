package com.samyak.tempboxbeta.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse<T> {
    @SerializedName("hydra:member")
    private List<T> members;
    
    @SerializedName("hydra:totalItems")
    private int totalItems;
    
    @SerializedName("hydra:view")
    private HydraView view;

    // Constructors
    public ApiResponse() {}

    // Getters and Setters
    public List<T> getMembers() { return members; }
    public void setMembers(List<T> members) { this.members = members; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

    public HydraView getView() { return view; }
    public void setView(HydraView view) { this.view = view; }

    // Inner class for pagination view
    public static class HydraView {
        @SerializedName("@id")
        private String id;
        
        @SerializedName("@type")
        private String type;
        
        @SerializedName("hydra:first")
        private String first;
        
        @SerializedName("hydra:last")
        private String last;
        
        @SerializedName("hydra:previous")
        private String previous;
        
        @SerializedName("hydra:next")
        private String next;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getFirst() { return first; }
        public void setFirst(String first) { this.first = first; }

        public String getLast() { return last; }
        public void setLast(String last) { this.last = last; }

        public String getPrevious() { return previous; }
        public void setPrevious(String previous) { this.previous = previous; }

        public String getNext() { return next; }
        public void setNext(String next) { this.next = next; }
    }
} 