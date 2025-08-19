
document.addEventListener("DOMContentLoaded", function () {
    const lastTab = localStorage.getItem("lastAdminTab") || "dashboard";
    showContent(lastTab);  // Show saved tab or default to dashboard
});

document.addEventListener("DOMContentLoaded", function() {
	    const userName = sessionStorage.getItem("userName");

	    // Redirect to login page if user session is null
	    if (userName === null) {
	        window.location.href = "/login";
	    }
	});
		   
	   document.getElementById('logout').addEventListener('click', function(event) {
	       event.preventDefault();

	       const userName = sessionStorage.getItem('userName') || 'User';
	       const logoutMessage = `Are you sure you want to logout from TimeX, "${userName}"?`;
	       document.getElementById('logoutMessage').innerText = logoutMessage;

	       const logoutModal = new bootstrap.Modal(document.getElementById('logoutConfirmModal'));
	       logoutModal.show();
	   });


	   document.getElementById('confirmLogoutBtn').addEventListener('click', function() {
	       sessionStorage.clear();
		   localStorage.clear();
	       window.location.href = '/login'; 
	   });


document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".nav-link").forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();
            showContent(this.dataset.section);
            setActiveNavLink(this);
            closeSidebarOnMobile(); 
        });
    });
 
    document.getElementById("sidebarToggle").addEventListener("click", toggleSidebar);
});

function showAlert(message, type = 'success') {
    const alertToast = document.getElementById('alertToast');
    const alertMessage = document.getElementById('alertMessage');
    
 
    alertMessage.textContent = message;
    alertToast.className = `toast align-items-center text-white bg-${type} border-0`;


    const toast = new bootstrap.Toast(alertToast, { delay: 3000 }); 
    toast.show();
}


function showLoader() {
    document.getElementById("loader").style.display = "flex";
}

function hideLoader() {
    document.getElementById("loader").style.display = "none";
}

function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    sidebar.classList.toggle("show");
}

function closeSidebarOnMobile() {
    if (window.innerWidth <= 768) {
        document.getElementById("sidebar").classList.remove("show");
    }
}





/*Script for dashboard icons functions */   
function showContent(section) {
	
  if (!section) return;
  
  localStorage.setItem("lastAdminTab", section);
  
    const title = document.getElementById("content-title");
    const contentBox = document.getElementById("content-box");
	const summery = document.getElementById("summery");
	

    const sections = {
        "dashboard": `
					
        `,
		"approval": `
		<div>
		    <button class="btn btn-info" id="showEmployeesBtn" onclick="listAssignedEmployees()">Show Assigned Employees</button>
		</div>
		<br>

		<!-- Search Box -->
		<input type="text" id="searchAssignedEmployee" class="form-control mb-2" 
		       placeholder="Search Assigned Employee..." style="display: none;">
			   			   			  
		<!-- Scrollable Assigned Employees Table -->
		<div style="max-height: 200px; overflow-y: auto; border: 1px solid #ddd; border-radius: 5px; display: none;" id="assignedEmployeeTableContainer">
		    <table id="assignedEmployeeTable" class="table table-bordered table-striped">
		        <thead class="table-dark">
		            <tr>
		                <th>Employee Name</th>
		                <th>Charge Code</th>
		                <th>Description</th>
		            </tr>
		        </thead>
		        <tbody id="assignedEmployeeTableBody">
		            <!-- Rows will be added dynamically -->
		        </tbody>
		    </table>
		</div> <br>
		
		
		<!-- Time Approval filters -->
		<div class="d-flex flex-wrap align-items-center gap-3 px-2 py-3 bg-light rounded shadow-sm">
		  
		  <!-- Employee Dropdown -->
		  <div class="dropdown">
		    <button class="btn btn-outline-primary dropdown-toggle" type="button" id="employeeDropdownBtn"
		            data-bs-toggle="dropdown" aria-expanded="false" style="min-width: 180px;">
		      Select Employee
		    </button>
		    <ul class="dropdown-menu shadow border-0" aria-labelledby="employeeDropdownBtn"
		        style="max-height: 300px; overflow: hidden; width: 400px; min-width: 250px;">
		      
		      <!-- Search Input -->
		      <li style="position: sticky; top: 0; background: white; z-index: 10; padding: 8px 10px;">
		        <input type="text" class="form-control form-control-sm" id="employeeSearchInput"
		               placeholder="Search employee..." onkeyup="filterEmployeeList()" autocomplete="off">
		      </li>

		      <!-- Employee List -->
		      <div id="employeeDropdownList" style="max-height: 220px; overflow-y: auto;"></div>
		    </ul>
		  </div>

		  <!-- Year Filter -->
		  <select id="yearFilter" class="form-select form-select-sm" style="width: 110px;" onchange="handleDateFilterChange(event)">
		    <option value="">Year</option>
		  </select>

		  <!-- Month Filter -->
		  <select id="monthFilter" class="form-select form-select-sm" style="width: 140px;" onchange="handleDateFilterChange(event)">
		    <option value="">Month</option>
		  </select>

		  <!-- Clear Filter Button -->
		  <button class="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1"
		          onclick="clearEmployeeFilter()" title="Clear Filter">
		    <i class="bi bi-x-circle"></i> Clear
		  </button>

		</div>

		<br>
	
		<div class="d-flex gap-3">
		    <div class="card text-center bg-warning text-white" style="cursor: pointer;" onclick="showSection('pending')">
		        <div class="card-body">
		            <h5 class="card-title">Pending</h5>
		            <h3 id="pendingCount">0</h3>
		        </div>
		    </div>

		    <div class="card text-center bg-success text-white" style="cursor: pointer;" onclick="showSection('approved')">
		        <div class="card-body">
		            <h5 class="card-title">Approved</h5>
		            <h3 id="approvedCount">0</h3>
		        </div>
		    </div>

		    <div class="card text-center bg-danger text-white" style="cursor: pointer;" onclick="showSection('issue')">
		        <div class="card-body">
		            <h5 class="card-title">Rejected</h5>
		            <h3 id="issueCount">0</h3>
		        </div>
		    </div>
		</div> <br> <br>


		<!-- Sections (Initially Hidden) -->
		<div id="pendingSection" style="display: none;">
		    <h3>Pending Approvals</h3>
		    
		    <!-- Search Input for Pending Approvals -->
		    <input type="text" id="searchPendingEmployee" class="form-control mb-2" 
		           placeholder="Search Pending Employee..." oninput="filterTable('pendingSummaryBody', this.value)">

		    <div id="pendingSummaryContent" class="border p-3 bg-light">
		        <table class="table table-bordered">
		            <thead>
		                <tr>
		                    <th>Employee</th>
		                    <th>Period</th>
		                    <th>Total Working Days<br>(Without Sunday's)</th>
		                    <th>Total Absences</th>
							<th>Loss of Pay</th>
		                    <th>Charge Code Details</th>
							<th>View</th>
		                    <th>Action</th>
		                </tr>
		            </thead>
		            <tbody id="pendingSummaryBody">
		                <!-- Data will be inserted here dynamically -->
		            </tbody>
		        </table>
		    </div>
		</div>

		<div id="approvedSection" style="display: none;">
		    <h3>Approved List</h3>

		    <!-- Search Input for Approved List -->
		    <input type="text" id="searchApprovedEmployee" class="form-control mb-2" 
		           placeholder="Search Approved Employee..." oninput="filterTable('approvedSummaryBody', this.value)">

		    <div id="approvedSummaryContent" class="border p-3 bg-light">
		        <table class="table table-bordered">
		            <thead>
		                <tr>
		                    <th>Employee</th>
		                    <th>Period</th>
		                   <th>Total Working Days <br>(Without Sunday's)</th>
		                    <th>Total Absences</th>
							<th>Loss of Pay</th>
		                    <th>Charge Code Details</th>
							<th>View</th>
		                </tr>
		            </thead>
		            <tbody id="approvedSummaryBody">
		                <!-- Data will be inserted here dynamically -->
		            </tbody>
		        </table>
		    </div>
		</div>

		<div id="issueSection" style="display: none;">
		    <h3>Rejected List</h3>

		    <!-- Search Input for Issued List -->
		    <input type="text" id="searchIssueEmployee" class="form-control mb-2" 
		           placeholder="Search Rejected Employee..." oninput="filterTable('issueSummaryBody', this.value)">

		    <div id="issueSummaryContent" class="border p-3 bg-light">
		        <table class="table table-bordered">
		            <thead>
		                <tr>
		                    <th>Employee</th>
		                    <th>Period</th>
		                    <th>Total Working Days <br>(Without Sunday's)</th>
		                    <th>Total Absences</th>
							<th>Loss of Pay</th>
		                    <th>Charge Code Details</th>
							<th>View</th>
		                </tr>
		            </thead>
		            <tbody id="issueSummaryBody">
		                <!-- Data will be inserted here dynamically -->
		            </tbody>
		        </table>
		    </div>
		</div>
		       `,
		"manage-user": `<button class="btn btn-info mb-3" id="addEmployeeBtn">Add Employee</button><div id="form-container"></div>
		     <div id="form-container"></div>
		         
		           <table class="table table-striped">
		               <thead>
		                   <tr>
						   	  <th>ID</th>
						   	   <th>Onboard Date</th>
		                       <th>Employee</th>
		                       <th>Email</th>
							   <th>Designation</th>
							   <th>Status</th>
		                       <th>Action</th>	
		                   </tr>
		               </thead>
		               <tbody id="employee-table-body"></tbody>
		           </table>
		   `,
		   "delegates": `<button class="btn btn-info mb-3" id="addDelegateBtn">Add Delegates</button><div id="form-container"></div>
		   <table class="table table-striped">
		   		   		   		<thead>
		   		   		   		   <tr>
		   		   		   				<th>ID</th>
		   		   		   		         <th>Delegator Name</th>
		   		   		   		          <th>Delegator Email</th>
		   		   		   		          <th>Action</th>
		   		   		   		     </tr>
		   		   		   		  </thead>
		   		   		   	 <tbody id="Delegate-table-body"></tbody>
		   		   		  </table>
		   `,
		   "charge-code": `<button class="btn btn-info mb-3" id="addChargeCodeBtn">Add Code</button><div id="form-container"></div> 
		
		   		           <table class="table table-striped">
		   		               <thead>
		   		                   <tr>
		   						   	  
		   						   	   <th>Code Type</th>
		   		                       <th>Code</th>
		   		                       <th>Client Name</th>
		   							   <th>Description</th>
		   		                       <th>Project Type</th>
									   <th>Start date </th>
									   <th>Country</th>
		   		                       <th>Action</th>
		   		                   </tr>
		   		               </thead>
		   		               <tbody id="code-table-body"></tbody>
		   		           </table>
		   `,
		   "expense-code": `<button class="btn btn-info mb-3" id="addExpenseCodeBtn">Add Expense Code</button><div id="form-container"></div>
		  
		   		  <table class="table table-striped">
		   		   		<thead>
		   		   		   <tr>
		   		   				
		   		   		         <th>Expense Code</th>
		   		   		          <th>Expense Type</th>
		   		   		          <th>Action</th>
		   		   		     </tr>
		   		   		  </thead>
		   		   	 <tbody id="Expense-table-body"></tbody>
		   		  </table>
		   		   
		    `,
			"holiday": `<button class="btn btn-info mb-3" id="addHolidayBtn">Add Holiday</button><div id="form-container"></div>
					     <div id="form-container"></div>
					         
					           <table class="table table-striped">
					               <thead>
					                   <tr>
									  		 <th>Name</th>
										   <th>Date</th>							
										   <th>Year</th>
					                       <th>Action</th>	
					                   </tr>
					               </thead>
					               <tbody id="holiday-table-body"></tbody>
					           </table>
					   `,
					   "salary-process": `<button class="btn btn-info mb-3" id="addSalarybtn">Add Salary </button><div id="form-container"></div>
					   	      <div id="form-container"></div>
					   					   		         
					   				<table class="table table-striped">
					   					   <thead>
					   					    <tr>
					   					           <th>ID</th>
					   					   		     <th>Employee Name</th>
					   					   		       <th>DOJ</th>
					   					                <th>Basic Salary</th>
														<th>Annual Salary</th>
														<th>Effective From</th>
														<th>Reason</th>
														<th>Bank Details</th>
					   					   		        <th>Action</th>	
					   					   	</tr>
					   				</thead>
					      <tbody id="Initialsalary-table-body"></tbody>
					  </table>
			         `,
					   "payslip": `
					   <h1>Payslip section</h1>	
					   <div class="container mt-4">
					     <h2 class="mb-4">Generate & Approve Payslip</h2>

					     <div class="row g-3 mb-4">
					       <div class="col-md-4">
					         <label class="form-label">Select Month</label>
					         <input type="month" class="form-control" id="monthPicker"  onchange="loadEmployeesForMonth()" />
					       </div>
					       <div class="col-md-4">
					         <label class="form-label">Select Employee</label>
					         <select class="form-select" id="employeeSelect" onchange="loadPayslipDetails()" >
					           <option value="">Select Employee</option>
					         </select>
					       </div>
					       <div class="col-md-4 d-flex align-items-end">
						   <button id="PayslipapproveBtn" class="btn btn-success" onclick="showPayslipApproveConfirmation()">Approve Payslip</button>
					       </div>
					     </div>

					     <div id="payslip-preview-container">
					       <!-- Dynamic payslip preview will be rendered here -->
					       <div class="card shadow-sm p-4 mb-4">
					         <h4 class="mb-3">Payslip Preview –  </h4>
					         <table class="table table-bordered">
					           <tbody>
					             <tr><th>Employee Name</th><td>-</td></tr>
					             <tr><th>Onboarded Date</th><td>-</td></tr>
					             <tr><th>Designation</th><td>-</td></tr>
								 <tr><th>Basic Salary</th><td>₹</td></tr>
								 <tr><th>STD Days</th><td>-</td></tr>
								 <tr><th>Worked Days</th><td>-</td></tr>
								 <tr><th>Paid Leaves</th><td>-</td></tr>					             								 
					             <tr><th>Loss of Pay</th><td>-</td></tr>					             
					             <tr><th>Deductions</th><td>₹</td></tr>
					             <tr class="table-success"><th>Net Pay</th><td><strong>₹</strong></td></tr>
					           </tbody>
					         </table>
					       </div>
					     </div>
					   </div>
					   
					   <div id="form-container"></div>
					   	         
					   	           <table class="table table-striped">
					   	               <thead>
					   	                   <tr>
					   					   	  <th>ID</th>
					   	                       <th>Employee</th>
					   						   <th>Designation</th>
											   <th>Basic Pay</th>
					   						   <th>Month</th>
					   	                       <th>STD Days</th>
											   <th>Worked Days</th>	
											   <th>Net Pay</th>	
											   <th>Bank</th>
											   <th>Salary Processed At</th>
											   <th>Payslip</th>
					   	                   </tr>
					   	               </thead>
					   	               <tbody id="payslip-table-body"></tbody>
					   	           </table>
					   	   
							 `
    };

    title.innerText = section.replace("-", " ").replace(/\b\w/g, l => l.toUpperCase());
    contentBox.innerHTML = sections[section] || "<p>Content Not Found</p>";

    attachFormListeners();
	
	if (section === "manage-user") {
	        fetchEmployeeData();
	    }
	else if(section === "charge-code") {
		fetchCodeDatas();
		
	}
	else if(section === "expense-code") {

		fetchExpense();
	}
	else if(section === "holiday") {
		fetchHoliday();
	}
	else if(section === "delegates") {
		fetchDelegator();
	}
	else if (section === "dashboard") {
	      
	        Promise.all([
	            fetch("/getEmployeesCount").then(res => res.json()),
	            fetch("/getChargecodesCount").then(res => res.json()),
	            fetch("/getExpensecodesCount").then(res => res.json()),
	            //fetch("/getDelegatorsCount").then(res => res.json())
	        ])
	        .then(([totalEmployees, totalChargeCodes, totalExpenseCodes, totalDelegators]) => {
	          
	            const dynamicDashboard = `
	               <div class="dashboard-card-box">
	                    ${createCard("bi-people", "Total Employees", totalEmployees)}
	                   
	                    ${createCard("bi-credit-card", "Charge Codes", totalChargeCodes)}
	                    ${createCard("bi bi-receipt", "Expense Codes", totalExpenseCodes)}
	                </div> <br>
					
	            `;

				
	            contentBox.innerHTML = dynamicDashboard;
				
				
			
	        }) 
	        .catch(error => console.error("Error fetching data for dashboard:", error));
			
			    }
				
				else if(section === "approval") {
						fetchCounts();
						loadDropdownEmployees();
						fetchFilteredTables();
						populateYearFilter();
						clearEmployeeFilter();
					}
					else if(section === "salary-process"){
						fetchinitialSalary();
					}
					else if(section === "payslip") {
						fetchPayslipData();
					}
						
				}
					
						
				/*Charge code assinges employees*/
				function listAssignedEmployees() {
				    const searchInput = document.getElementById("searchAssignedEmployee");
				    const employeeTableBody = document.getElementById("assignedEmployeeTableBody");
				    const tableContainer = document.getElementById("assignedEmployeeTableContainer");

				
				    if (tableContainer.style.display === "block") {
				        searchInput.style.display = "none";
				        tableContainer.style.display = "none";
				        return; 
				    }

				    fetch("/assigned-employees") 
				        .then(response => response.json())
				        .then(data => {
				            employeeTableBody.innerHTML = ""; 

				            data.forEach(employee => {
				                const row = document.createElement("tr");

				                row.innerHTML = `
				                    <td>${employee.employeeName}</td>
				                    <td>${employee.chargeCode}</td>
				                    <td>${employee.description}</td>
				                `;

				                employeeTableBody.appendChild(row);
				            });

				        
				            searchInput.style.display = "block";
				            tableContainer.style.display = "block";
				        })
				        .catch(error => console.error("❌ Error fetching employees:", error));

				
				    searchInput.addEventListener("keyup", function () {
				        const filter = searchInput.value.toLowerCase();
				        const rows = employeeTableBody.getElementsByTagName("tr");

				        Array.from(rows).forEach(row => {
				            const text = row.textContent.toLowerCase();
				            row.style.display = text.includes(filter) ? "" : "none";
				        });
				    });
				}

				
				function filterTable(tableId, searchValue) {
				    searchValue = searchValue.toLowerCase().trim();
				    let tableRows = document.querySelectorAll(`#${tableId} tr`);

				    tableRows.forEach(row => {
				        let username = row.cells[0].textContent.toLowerCase(); 

				        if (username.includes(searchValue)) {
				            row.style.display = ""; 
				        } else {
				            row.style.display = "none";
				        }
				    });
				}

					
			function showSection(section) {
					document.getElementById("pendingSection").style.display = "none";
					 document.getElementById("approvedSection").style.display = "none";
					document.getElementById("issueSection").style.display = "none";

					if (section === "pending") {
						document.getElementById("pendingSection").style.display = "block";
					} else if (section === "approved") {
							document.getElementById("approvedSection").style.display = "block";
					} else if (section === "issue") {
							document.getElementById("issueSection").style.display = "block";
					 }
				}

					document.addEventListener("DOMContentLoaded", function () {
														    				
				if (
				document.getElementById("issueCount") &&
				document.getElementById("approvedCount") &&
				document.getElementById("pendingCount")
								
					) {
							        
						 fetchCounts();
						 fetchFilteredTables();
					 } 
				});


							
				// ⬇️ Actual count fetch logic with year + month + employee
				function fetchCounts() {
				    let url = '/counts';
				    const params = [];

				    if (selectedUsername) params.push(`employee=${encodeURIComponent(selectedUsername)}`);
				    if (selectedYear) params.push(`year=${selectedYear}`);
				    if (selectedMonth) params.push(`month=${selectedMonth}`);

				    if (params.length > 0) {
				        url += "?" + params.join("&");
				    }

				    fetch(url)
				        .then(response => response.json())
				        .then(data => {
				            document.getElementById("pendingCount").textContent = data.pending ?? 0;
				            document.getElementById("approvedCount").textContent = data.approved ?? 0;
				            document.getElementById("issueCount").textContent = data.issue ?? 0;
				        })
				        .catch(error => console.error("❌ Error fetching counts:", error));
				}


								/*Filtering the employees*/
								let selectedUsername = null;
								let selectedYear = null;
								let selectedMonth = null;

								// ⬇️ Load employees into dropdown
								function loadDropdownEmployees() {
								    fetch("/getEmployees")
								        .then(res => res.json())
								        .then(data => {
								            const listContainer = document.getElementById("employeeDropdownList");
								            listContainer.innerHTML = "";

								            data.forEach(emp => {
								                if (emp.status === 'active') {
								                    const item = document.createElement("li");
								                    item.className = "dropdown-item employee-item";
								                    item.textContent = `${emp['E-name']} - ${emp['E-desg']}`;
								                    item.setAttribute("data-name", (emp['E-name'] + ' ' + emp['E-desg']).toLowerCase());
								                    item.onclick = () => selectEmployee(emp['E-name'], emp['E-desg']);
								                    listContainer.appendChild(item);
								                }
								            });
								        });
								}

								// ⬇️ Filter employees inside dropdown
								function filterEmployeeList() {
								    const query = document.getElementById("employeeSearchInput").value.toLowerCase();
								    const items = document.querySelectorAll(".employee-item");

								    items.forEach(item => {
								        const name = item.getAttribute("data-name");
								        item.style.display = name.includes(query) ? "block" : "none";
								    });
								}

								// ⬇️ Select employee logic
								function selectEmployee(name, desg) {
								    selectedUsername = name;
								    document.getElementById("employeeDropdownBtn").innerText = `${name} - ${desg}`;
								    document.getElementById("employeeSearchInput").value = "";
								    filterEmployeeList();

								    fetchCounts();
								    fetchFilteredTables();
								}

								// ⬇️ Clear employee + filter state
								function clearEmployeeFilter() {
								    selectedUsername = null;
								    selectedYear = null;
								    selectedMonth = null;

								    // Reset Employee Dropdown
								    document.getElementById("employeeDropdownBtn").innerText = "Select Employee";
								    document.getElementById("employeeSearchInput").value = "";
								    filterEmployeeList();

								    // Reset Year and Month Dropdowns
								    const yearSelect = document.getElementById("yearFilter");
								    const monthSelect = document.getElementById("monthFilter");

								    yearSelect.value = "";
								    monthSelect.innerHTML = '<option value=""> Month </option>'; // Reset month dropdown

								    // Refresh year filter options
								    populateYearFilter();

								    // Show all cards & fetch all data
								    showAllCards();
								    fetchCounts();
								    fetchFilteredTables();
								}


								// ⬇️ Show all cards logic
								function showAllCards() {
								    const allCards = document.querySelectorAll('.card-container');
								    allCards.forEach(card => card.style.display = "block");
								}

							

								function populateYearFilter() {
								    const yearSelect = document.getElementById("yearFilter");
								    const currentYear = new Date().getFullYear();

								    yearSelect.innerHTML = '<option value="">Year</option>';
								    for (let year = currentYear; year >= currentYear - 5; year--) {
								        let option = document.createElement("option");
								        option.value = year;
								        option.textContent = year;
								        yearSelect.appendChild(option);
								    }
								}

								function populateMonthFilter(selectedYear) {
								    const monthSelect = document.getElementById("monthFilter");
								    const currentDate = new Date();
								    const currentYear = currentDate.getFullYear();
								    const currentMonth = currentDate.getMonth() + 1;

								    const months = [
								        { value: "01", label: "January" },
								        { value: "02", label: "February" },
								        { value: "03", label: "March" },
								        { value: "04", label: "April" },
								        { value: "05", label: "May" },
								        { value: "06", label: "June" },
								        { value: "07", label: "July" },
								        { value: "08", label: "August" },
								        { value: "09", label: "September" },
								        { value: "10", label: "October" },
								        { value: "11", label: "November" },
								        { value: "12", label: "December" }
								    ];

								    monthSelect.innerHTML = '<option value="">Month</option>';

								    months.forEach((month, index) => {
								        if (selectedYear === String(currentYear) && index + 1 > currentMonth) return;
								        const option = document.createElement("option");
								        option.value = month.value;
								        option.textContent = month.label;
								        monthSelect.appendChild(option);
								    });
								}

								function handleDateFilterChange(event) {
								    selectedYear = document.getElementById("yearFilter").value;
								    selectedMonth = document.getElementById("monthFilter").value;

								    if (event.target.id === "yearFilter") {
								        populateMonthFilter(selectedYear);
								        document.getElementById("monthFilter").value = "";
								        selectedMonth = null;
								    }

								    fetchCounts();
									fetchFilteredTables();
								}

								function fetchFilteredTables() {
								    const selectedYear = document.getElementById("yearFilter").value;
								    const selectedMonth = document.getElementById("monthFilter").value;

								    function filterByEmployeeMonthYear(data) {
								        return data.filter(entry => {
								            const matchesEmployee = !selectedUsername || entry.username === selectedUsername;

								            // Extract month and year from entry.period
								            if (entry.period && entry.period.length >= 10) {
								                const periodMonth = entry.period.substring(3, 5);  // MM
								                const periodYear = entry.period.substring(6, 10);  // YYYY

								                const matchesYear = !selectedYear || periodYear === selectedYear;
								                const matchesMonth = !selectedMonth || periodMonth === selectedMonth;

								                return matchesEmployee && matchesYear && matchesMonth;
								            }

								            return false;
								        });
								    }

								    fetch("/getPendingApprovals")
								        .then(response => response.json())
								        .then(data => {
								            const filteredData = filterByEmployeeMonthYear(data);
								            populateTable("pendingSummaryBody", filteredData, "Pending");
								            paginateTable("pendingSummaryBody");
								        });

								    fetch("/getApprovalslist")
								        .then(response => response.json())
								        .then(data => {
								            const filteredData = filterByEmployeeMonthYear(data);
								            populateTable("approvedSummaryBody", filteredData, "Approved");
								            paginateTable("approvedSummaryBody");
								        });

								    fetch("/getIssuelist")
								        .then(response => response.json())
								        .then(data => {
								            const filteredData = filterByEmployeeMonthYear(data);
								            populateTable("issueSummaryBody", filteredData, "Issue");
								            paginateTable("issueSummaryBody");
								        });
								}

							
/*
							function formatPeriod(periodStr) {
							    if (!periodStr || !periodStr.includes(" - ")) return periodStr;

							    const [start, end] = periodStr.split(" - ");
							    const startDateParts = start.split("/");
							    const endDateParts = end.split("/");

							    if (startDateParts.length !== 3 || endDateParts.length !== 3) return periodStr;

							    const startDate = new Date(`${startDateParts[2]}-${startDateParts[1]}-${startDateParts[0]}`);
							    const endDate = new Date(`${endDateParts[2]}-${endDateParts[1]}-${endDateParts[0]}`);

							    if (isNaN(startDate) || isNaN(endDate)) return periodStr;

							    const format = (date) => {
							        const day = String(date.getDate()).padStart(2, '0');
							        const month = date.toLocaleString('default', { month: 'short' });
							        const year = date.getFullYear();
							        return `${day}-${month}-${year}`;
							    };

							    return `${format(startDate)} to ${format(endDate)}`;
								
								<td><b id="period-${entry.username}-${entry.period}">${formatPeriod(entry.period)}</b></td>
							} */


							
							
							function populateTable(tableId, data, status) {
						
							    let tableBody = document.getElementById(tableId);

							    if (!tableBody) {
							        console.error(`Error: Table with ID '${tableId}' not found.`);
							        return;
							    }

							    tableBody.innerHTML = ""; 

							    if (!Array.isArray(data) || data.length === 0) {
							        tableBody.innerHTML = `<tr><td colspan="6">No data available</td></tr>`;
							        return;
							    }

							    data.forEach(entry => {
							        let row = document.createElement("tr");
								

							        row.innerHTML = `
							            <td>${entry.username}</td>										
										<td><b id="period-${entry.username}-${entry.period}">${entry.period}</b></td>						
							            <td id="hours-${entry.username}-${entry.period}">Fetching...</td>
							            <td id="absences-${entry.username}-${entry.period}">Fetching...</td>
										<td id="paid-${entry.username}-${entry.period}">Fetching...</td>
							            <td id="charge-${entry.username}-${entry.period}">Fetching...</td>
										<td class="text-center">																		  
										  <button class="btn btn-info btn-sm" title="View Timesheet" onclick="viewTimesheetGrid('${entry.username}', '${entry.period}')">
										      <i class="bi bi-eye"></i>
										  </button>
										</td>

							            ${status === "Pending" ? `
							                <td style="display: flex; gap: 10px;">
					
							                        <button class="btn btn-success btn-sm" title="Approve Timesheet" onclick="handleApproval('${entry.username}', '${entry.period}')"><i class="bi bi-check2-square"></i></button>
							                        <button class="btn btn-danger btn-sm"  title="Reject Timesheet" onclick="handleIssue('${entry.username}', '${entry.period}')"><i class="bi bi-file-x"></i></button>
							     
							                </td>
							            ` : ""}
							        `;

							        tableBody.appendChild(row);
																        
							        fetchEmployeeSummary(entry.username, entry.period, status);
							    });
								
								// Optional: Remove old pagination if re-rendering
								   const oldPagination = document.getElementById(`${tableId}-pagination`);
								   if (oldPagination) oldPagination.remove();
								
								
								const searchInput = document.getElementById("searchEEmployee");

								if (searchInput) {
									searchInput.addEventListener("keyup", function () {
										const filter = searchInput.value.toLowerCase();
										const rows = employeeTableBody.getElementsByTagName("tr");

										Array.from(rows).forEach(row => {
											const text = row.textContent.toLowerCase();
											row.style.display = text.includes(filter) ? "" : "none";
										});
									});
								} 

							}

							function fetchEmployeeSummary(username, period, status) {
							    

							    let url = `/getSummary?username=${encodeURIComponent(username)}&period=${encodeURIComponent(period)}&status=${encodeURIComponent(status)}`;
							    
							    fetch(url)
							        .then(response => response.json())
							        .then(summary => {
							            if (!summary) {
							                console.error("No summary data received");
							                return;
							            }

							            document.getElementById(`hours-${username}-${period}`).textContent = (summary.totalHours - summary.totalAbsences)/9   || "0";
							            document.getElementById(`absences-${username}-${period}`).textContent = summary.totalAbsences/9 || "0";					
										document.getElementById(`paid-${username}-${period}`).textContent = summary.paidLeaveDays || "0";
										

							          
							            let chargeCell = document.getElementById(`charge-${username}-${period}`);
							            chargeCell.innerHTML = summary.entries.length > 0
							                ? summary.entries.map(entry => `<div class="charge-code-item">${entry.chargeCode} - ${entry.hours} hrs</div>`).join("")
							                : "No data";
							        })
							        .catch(error => {
							            console.error("Error fetching summary data:", error);
							        });
							}


							
							
							function getTableBodyByStatus(status) {
							    if (status === "Approved") return document.getElementById("approvedSummaryBody");
							    if (status === "Issue") return document.getElementById("issueSummaryBody");
							    return document.getElementById("pendingSummaryBody"); // Default to pending
							}


						
							function handleApproval(username, period) {
							    selectedUsername = username;
							    selectedPeriod = period;

							    document.getElementById("approvalMessage").innerText = 
							        `Are you sure you want to approve ${selectedUsername}'s timesheet for ${selectedPeriod}?`;

							    let approvalModal = new bootstrap.Modal(document.getElementById("approvalConfirmModal"));
							    approvalModal.show();
							}

							
							document.getElementById("confirmApprovalBtn").addEventListener("click", function () {
							    fetch("/approve", {
							        method: "POST",
							        headers: { "Content-Type": "application/json" },
							        body: JSON.stringify({ username: selectedUsername, period: selectedPeriod })
							    })
							    .then(response => response.json())
							    .then(result => {
							        showAlert(result.message, "success");

							        // ✅ Reset filters to show full data
							        selectedUsername = null;

							        fetchCounts();
							        fetchFilteredTables();
							    })
							    .catch(error => console.error("Error approving timesheet:", error));

							    let approvalModal = bootstrap.Modal.getInstance(document.getElementById("approvalConfirmModal"));
							    approvalModal.hide();
							});


							
							function handleIssue(username, period) {
							    selectedUsername = username;
							    selectedPeriod = period;

							    let issueModal = new bootstrap.Modal(document.getElementById("issueMessageModal"));
							    issueModal.show();
							}

							
							document.getElementById("confirmIssueBtn").addEventListener("click", function () {
							    let issueMessage = document.getElementById("issueMessageInput").value.trim();

							    if (!issueMessage) {
							        document.getElementById("issueError").style.display = "block";
							        return;
							    }
							    document.getElementById("issueError").style.display = "none";

							    fetch("/raiseIssue", {
							        method: "POST",
							        headers: { "Content-Type": "application/json" },
							        body: JSON.stringify({ username: selectedUsername, period: selectedPeriod, issueMessage })
							    })
							    .then(response => response.json())
							    .then(result => {
							        showAlert(result.message, "success");
									
									selectedUsername = null;

									fetchCounts();
									fetchFilteredTables();
							    })
							    .catch(error => console.error("Error raising issue:", error));

							    let issueModal = bootstrap.Modal.getInstance(document.getElementById("issueMessageModal"));
							    issueModal.hide();
								document.getElementById("issueMessageInput").value = "";
							});

			
								function updateEmployeeButton(username, period, status) {
								    sessionStorage.setItem(`approvalStatus_${username}_${period}`, status);
								}


	
									function viewTimesheetGrid(username, period) {
										    fetch(`/timesheetGrid?username=${encodeURIComponent(username)}&period=${encodeURIComponent(period)}`)
										        .then(response => response.json())
										        .then(data => {
										            // Set modal header info
										            document.getElementById("modalUsername").textContent = username;
										            document.getElementById("modalPeriod").textContent = period;

										            updateAdminTimesheetHeader(period);
										            fillAdminTimesheetTable(data, period);
										            new bootstrap.Modal(document.getElementById("adminTimesheetModal")).show();
										        })
										        .catch(err => {
										            console.error("Error fetching grid:", err);
										            alert("Failed to load timesheet data");
										        });
										}



										function updateAdminTimesheetHeader(period) {
										    const theadRow = document.getElementById("adminTimesheetHeader");
										    theadRow.innerHTML = `<th class="text-white">Charge code</th>`;

										    const [startStr, endStr] = period.split(" - ");
										    const [startDay, startMonth, startYear] = startStr.split("/").map(Number);
										    const [endDay, endMonth, endYear] = endStr.split("/").map(Number);

										    const startDate = new Date(startYear, startMonth - 1, startDay);
										    const endDate = new Date(endYear, endMonth - 1, endDay);

										    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
										        console.error("Invalid period format in updateAdminTimesheetHeader", { startStr, endStr });
										        return;
										    }

										    for (let d = startDate.getDate(); d <= endDate.getDate(); d++) {
										        const padded = d.toString().padStart(2, '0');
										        theadRow.innerHTML += `<th>${padded}</th>`;
										    }
										}

										
										function fillAdminTimesheetTable(data, period) {
										    const tbody = document.getElementById("adminTimesheetBody");
										    tbody.innerHTML = "";

										    // Calculate total columns using period string
										    const [startStr, endStr] = period.split(" - ");
										    const [startDay, startMonth, startYear] = startStr.split("/").map(Number);
										    const [endDay, endMonth, endYear] = endStr.split("/").map(Number);
										    const startDate = new Date(startYear, startMonth - 1, startDay);
										    const endDate = new Date(endYear, endMonth - 1, endDay);

										    let totalCols = 0;
										    const dateCursor = new Date(startDate);
										    while (dateCursor <= endDate) {
										        totalCols++;
										        dateCursor.setDate(dateCursor.getDate() + 1);
										    }
										  
										    const columnHasValue = Array(totalCols + 1).fill(false); // [0..totalCols]

										    const staticRowLabels = {
										        "0": "Work Location",
										        "1": "Company Code"
										    };

										    const allRows = [];

										    // --- Static Rows ---
										    Object.keys(staticRowLabels).forEach(rowKey => {
										        const row = document.createElement("tr");
										        row.innerHTML = `<td><b>${staticRowLabels[rowKey]}</b></td>`;

										        for (let col = 1; col <= totalCols; col++) {
										            const cell = data[rowKey]?.[col.toString()];
										            const [code, hrs] = cell ? cell.split("|") : ["", ""];
										            if (hrs) columnHasValue[col] = true;

										            row.innerHTML += `<td>${hrs ? `<div>${hrs}</div>` : ""}</td>`;
										        }

										        allRows.push(row);
										    });

										    // --- Dynamic Charge Code Rows ---
										    const dynamicRowKeys = Object.keys(data)
										        .filter(key => key !== "0" && key !== "1")
										        .sort((a, b) => parseInt(a) - parseInt(b));

										    dynamicRowKeys.forEach(rowKey => {
										        let chargeCodeTitle = "";
										        for (let col = 1; col <= totalCols; col++) {
										            const cell = data[rowKey]?.[col.toString()];
										            if (cell) {
										                const [code] = cell.split("|");
										                if (code) {
										                    chargeCodeTitle = code;
										                    break;
										                }
										            }
										        }

										        const row = document.createElement("tr");
										        row.innerHTML = `<td><b>${chargeCodeTitle || "Select Charge Code"}</b></td>`;

										        for (let col = 1; col <= totalCols; col++) {
										            const cell = data[rowKey]?.[col.toString()];
										            const [, hrs] = cell ? cell.split("|") : ["", ""];
										            if (hrs) columnHasValue[col] = true;

										            row.innerHTML += `<td>${hrs ? `<div>${hrs}</div>` : ""}</td>`;
										        }

										        allRows.push(row);
										    });

										    // --- Append All Rows ---
										    allRows.forEach(row => tbody.appendChild(row));

										    // --- Mark Empty Columns as Grey ---
										    const rows = tbody.querySelectorAll("tr");
										    rows.forEach(row => {
										        const cells = row.querySelectorAll("td");
										        for (let col = 1; col <= totalCols; col++) {
										            if (!columnHasValue[col]) {
										                cells[col].classList.add("bg-light", "text-muted");
										            }
										        }
										    });
										}
										

function setActiveNavLink(activeLink) {
    document.querySelectorAll(".nav-link").forEach(link => link.classList.remove("active"));
    activeLink.classList.add("active");
}

function createCard(icon, title, value) {
    return `
	<div class="dashboard-card">
	           <div class="card-icon">
	               <i class="${icon}"></i>
	           </div>
	           <div class="card-title">${title}</div>
	           <div class="card-value">${value}</div>
	       </div>
    `;
}

function attachFormListeners() {
	
    document.getElementById("addEmployeeBtn")?.addEventListener("click", () => showForm("employee"));
    document.getElementById("addDelegateBtn")?.addEventListener("click", () => showForm("delegates"));
    document.getElementById("addChargeCodeBtn")?.addEventListener("click", showDropdown);
	document.getElementById("addExpenseCodeBtn")?.addEventListener("click", () => showForm("Expense-code"));
	document.getElementById("addHolidayBtn")?.addEventListener("click", () => showForm("Holiday"));
	document.getElementById("addSalarybtn")?.addEventListener("click", () => showForm("addSalary"));
	
}

function showDropdown() {
    const formContainer = document.getElementById("form-container");
    formContainer.innerHTML = `
        <div class="mb-3">
            <label class="form-label">Select Code Type</label>
            <select class="form-control" id="codeType" onchange="handleCodeSelection()">
                <option value="">Select</option>
                <option value="charge-code">Charge Code</option>
                <option value="leave-code">Leave Code</option>
            </select>
        </div>
    `;
}

function handleCodeSelection() {
    const selectedValue = document.getElementById("codeType").value;
	if (selectedValue) {
	        showForm(selectedValue);
	    }
}



function showForm(type) {
    const formContainer = document.getElementById("form-container");
    formContainer.innerHTML = createForm(type);
	
			setTimeout(() => {
		        if (type === "addSalary") {
		            fetchEmployeesForSalaryForm();
		        }
		    }, 50);
	
	document.querySelector("#form-container form").addEventListener("submit", handleFormSubmit);
}

/*Function for sending data into backend (Java) */
function handleFormSubmit(event) {
    event.preventDefault(); 
    showLoader();

    const form = event.target;
    const formData = new FormData(form);
    const jsonData = Object.fromEntries(formData.entries());

    fetch(form.action, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(jsonData)
    })
	.then(async response => {
	        const data = await response.text();

	        if (!response.ok) {
	            // Handle specific error messages from backend
	            showAlert(data , "danger");
	            throw new Error(data);
	        }

	        // Success
	        showAlert(data, "success");
	        hideForm(); 

        // ✅ Refresh the right section
        const action = form.getAttribute("action");

        if (action.includes("addEmployee")) {
            fetchEmployeeData();
        } else if (action.includes("addChargeCode")) {
            fetchCodeDatas();
        } else if (action.includes("addDelegate")) {
            fetchDelegator();
        } else if (action.includes("addExpense")) {
            fetchExpense();
        } else if (action.includes("addHoliday")) {
            fetchHoliday();
        } else if (action.includes("addSalary")) {
            fetchinitialSalary();
        }
    })
    .catch(error => {
        console.error("Error:", error);
    })
    .finally(() => hideLoader());
}


function hideForm() {
    document.getElementById("form-container").innerHTML = "";
}



/*Funtion for fetching Data form backend (Java)*/
		

/* Fetch Employee Data */
function fetchEmployeeData() {
    fetch("/getEmployees")
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("employee-table-body");
            tableBody.innerHTML = "";
            data.forEach(employee => {
				
				const dropdownMenu = `
				    <div class="dropdown">
				        <button class="btn btn-sm btn-success" type="button" data-bs-toggle="dropdown" aria-expanded="false">
				            <i class="bi bi-three-dots-vertical"></i>
				        </button>
				        <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0" style="min-width: 200px; border-radius: 12px;">
				            <li>
				                <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="editEmployee('${employee.id}')">
				                    <i class="bi bi-pencil-square text-primary"></i>
				                    <span>Edit Employee</span>
				                </a>
				            </li>
				            <li>
				                <a class="dropdown-item d-flex align-items-center gap-2 py-2 text-${employee.status === 'active' ? 'danger' : 'success'}"
				                   href="#" onclick="employeeAction('${employee.id}', '${employee.status}')">
				                    <i class="bi ${employee.status === 'active' ? 'bi-person-x-fill' : 'bi-person-check-fill'}"></i>
				                    <span>${employee.status === 'active' ? 'Deactivate' : 'Activate'}</span>
				                </a>
				            </li>
				        </ul>
				    </div>
				`;

				  const dateObj = new Date(employee.onborad);
				  const formattedDate = `${dateObj.getFullYear()}-${dateObj.toLocaleString('default', { month: 'short' })}-${String(dateObj.getDate()).padStart(2, '0')}`;

                tableBody.innerHTML += `
                    <tr>
                        <td>${employee.id}</td>
                         <td>${formattedDate}</td>
                        <td>${employee['E-name']}</td>
                        <td>${employee['E-mail']}</td>
                        <td>${employee['E-desg']}</td>
                        <td>${employee.status}</td>
                        <td>${dropdownMenu}</td>
                    </tr>
                `;
            });

 
			// Populate Assign Employee Dropdown
			const employeeListContainer = document.getElementById("employeeList");
			employeeListContainer.innerHTML = ""; 
			data.forEach(employee => {
			   
			    if (employee.status === 'active') {
			        const li = document.createElement("li");
			        li.innerHTML = `<a class="dropdown-item" href="#" onclick="addEmployeetofield('${employee['E-name']}', '${employee['E-desg']}')">${employee['E-name']} - ${employee['E-desg']}</a>`;
			        employeeListContainer.appendChild(li);
			    }
			});
			paginateTable("employee-table-body");
        })
        .catch(error => console.error("Error fetching employees:", error));
}

function editEmployee(employeeid) {
    fetch(`/getEmployeeById/${employeeid}`)
        .then(response => response.json())
        .then(data => {
            

            const editFormHTML = `
                <div class="card p-3 mb-3">
                    <h4>Edit Employee Details</h4>
                    <form onsubmit="submitUpdatedEmployee(event, '${data.id}')">  
                        ${inputField("Name", "text", "E-name", "", data["E-name"])}
                        ${inputField("Email", "email", "E-mail", "", data["E-mail"])}
                        ${inputField("Designation", "text", "E-desg", "", data["E-desg"])}
						${inputField("Onboard date", "date", "onborad", "", data.onborad)}
                        ${selectField("Role", "E-role", ["Admin", "Employee"], data["E-role"])}
                        ${formButtons("Update")}
                    </form>
                </div>
            `;
            document.getElementById("form-container").innerHTML = editFormHTML;
        })
        .catch(error => console.error("Error fetching employee for edit:", error));
}


function submitUpdatedEmployee(event, id) {
    event.preventDefault();
    showLoader(); 

    const updatedData = {
        id: id,
        "E-name": document.getElementById("E-name").value,
        "E-mail": document.getElementById("E-mail").value,
        "E-desg": document.getElementById("E-desg").value,
        "E-role": document.getElementById("E-role").value,
        "onborad": document.getElementById("onborad").value,
    };

    fetch("/updateEmployee", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedData),
    })
    .then(async response => {
        const message = await response.text();

        if (!response.ok) {
            showAlert(message, "danger");
            throw new Error(message);
        }

        hideForm();
        showAlert(message, "success");
        fetchEmployeeData();
    })
    .catch(error => {
        console.error("Error updating employee:", error);
    })
    .finally(() => hideLoader());
}



/* Function to handle employee status action */
function employeeAction(employeeId, currentStatus) {
    const confirmationMessage = document.getElementById("confirmationMessage");
    const confirmActionBtn = document.getElementById("confirmActionBtn");

    // Set the confirmation message based on the current status
    if (currentStatus === 'active') {
        confirmationMessage.innerText = "Do you want to deactivate this employee?";
    } else {
        confirmationMessage.innerText = "Do you want to activate this employee?";
    }

    // Show the confirmation modal
    const confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModal'));
    confirmationModal.show();

	confirmActionBtn.onclick = function() {
	    fetch(`/updateEmployeeStatus/${employeeId}`, {
	        method: "PUT"
	    })
	    .then(response => {
	        if (response.ok) {
	            showAlert("Employee status updated successfully!", "success");
	            fetchEmployeeData();  // Refresh the table after update
	        } else {
	            showAlert("Failed to update employee status.", "danger");
	        }
	    })
	    .catch(error => {
	        console.error("Error updating employee status:", error);
	    })
	    .finally(() => {
	        document.activeElement.blur();     
	        confirmationModal.hide();          
	    });
	};
}




/* Fetch Charge Codes */
function fetchCodeDatas() {
    fetch("/getChargecodes") 
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("code-table-body");
            tableBody.innerHTML = ""; 
            
            data.forEach(code => {

                let rowClass = code.status.toLowerCase() === "complete" ? 'class="table-success"' : '';

              
				let dropdownMenu = (code.codeType.toLowerCase() === "charge code" && code.status.toLowerCase() === "progress") ? `
				  <div class="dropdown">
				    <button class="btn btn-sm btn-success" type="button" id="dropdownMenu${code.id}" data-bs-toggle="dropdown" aria-expanded="false">
				      <i class="bi bi-three-dots-vertical"></i>
				    </button>
				    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0" aria-labelledby="dropdownMenu${code.id}" style="min-width: 220px; border-radius: 12px;">
				      <li>
				        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="completeChargeCode('${code.id}')">
				          <i class="bi bi-check2-circle text-success"></i>
				          <span>Mark Complete</span>
				        </a>
				      </li>
				      <li>
				        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="openAssignModal('${code.id}', '${code.code}', '${code.description}')">
				          <i class="bi bi-person-plus-fill text-primary"></i>
				          <span>Assign to</span>
				        </a>
				      </li>
				      <li>
				        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="editChargeCode('${code.id}')">
				          <i class="bi bi-pencil-square text-warning"></i>
				          <span>Edit</span>
				        </a>
				      </li>
				      <li>
				        <a class="dropdown-item d-flex align-items-center gap-2 py-2 text-danger" href="#" onclick="deleteChargeCode('${code.id}')">
				          <i class="bi bi-trash-fill"></i>
				          <span>Delete</span>
				        </a>
				      </li>
				    </ul>
				  </div>
				` : '';			
				
				let formattedStartDate = code.startDate;

				// ✅ Check if it's a valid date
				if (code.startDate && code.startDate !== "-") {
				    const dateObj = new Date(code.startDate);
				    if (!isNaN(dateObj)) {
				        formattedStartDate = `${dateObj.getFullYear()}-${dateObj.toLocaleString('default', { month: 'short' })}-${String(dateObj.getDate()).padStart(2, '0')}`;
				    }
				}
	
                tableBody.innerHTML += `
                    <tr ${rowClass}>
                      
                        <td>${code.codeType}</td>
                        <td>${code.code}</td>
                        <td>${code.clientName}</td>
                        <td>${code.description}</td>
                        <td>${code.projectType}</td>
						<td>${formattedStartDate}</td>
                        <td>${code.country}</td>
                        <td>${dropdownMenu}</td>
                    </tr>
                `;
            });
			paginateTable("code-table-body");
        })
        .catch(error => console.error("Error fetching Charge codes:", error));
}



function editChargeCode(codeId) {
    fetch(`/getChargecodeById/${codeId}`)
        .then(response => response.json())	
        .then(data => {
			
            const editFormHTML = `
                <div class="card p-3 mb-3">
                    <h4>Edit Charge Code</h4>
                    <form onsubmit="submitUpdatedChargeCode(event, '${data.id}')">  
                        ${selectField("Code Type", "codeType", ["Charge code"], data.codeType)}        
                        ${selectField("Project Type", "projectType", ["External", "Internal"], data.projectType)}
                        ${inputField("Client/Organization", "text", "clientName", "", data.clientName)}
                        ${inputField("Onboard/Start date", "date", "startDate", "", data.startDate)}
                        ${inputField("Country/Region", "text", "country", "", data.country)}
                        ${textareaField("Description", "description", data.description)}
                        ${inputField("Charge Code", "text", "code", "charge-code", data.code, true)}
                        ${formButtons("Update")}
                    </form>
                </div>
            `;
            document.getElementById("form-container").innerHTML = editFormHTML;
        })
        .catch(error => console.error("Error fetching charge code for edit:", error));
}

function submitUpdatedChargeCode(event, id) {
    event.preventDefault();
    const updatedData = {
        id: id,
        codeType: document.getElementById("codeType").value,
        projectType: document.getElementById("projectType").value,
        clientName: document.getElementById("clientName").value,
        startDate: document.getElementById("startDate").value,
        country: document.getElementById("country").value,
        description: document.getElementById("description").value,
        code: document.getElementById("code").value,
    };

    fetch("/updateChargeCode", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedData),
    })
    .then(response => response.text())
    .then(message => {
		hideForm();
        showAlert(message,"success");
        fetchCodeDatas(); 
    })
    .catch(error => console.error("Error updating charge code:", error));
}


let chargeCodeToDelete = null; 

function deleteChargeCode(codeId) {
    chargeCodeToDelete = codeId; 
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    deleteModal.show();
}


document.getElementById("confirmDeleteBtn").addEventListener("click", function () {
    if (chargeCodeToDelete) {
        fetch(`/deleteChargeCode/${chargeCodeToDelete}`, {
            method: "DELETE",
        })
        .then(response => response.text())
        .then(message => {
            showAlert(message, "success");
            fetchCodeDatas();
        })
        .catch(error => console.error("Error deleting charge code:", error))
        .finally(() => {
            chargeCodeToDelete = null;
            const modal = bootstrap.Modal.getInstance(document.getElementById('deleteModal'));
            modal.hide();
        });
    }
});


let selectedChargeCodeId = null; 


function completeChargeCode(id) {
    selectedChargeCodeId = id; 
    var completeModal = new bootstrap.Modal(document.getElementById("completeModal"));
    completeModal.show(); 
}


document.getElementById("confirmCompleteBtn").addEventListener("click", function() {
    if (selectedChargeCodeId) {
        fetch(`/completeChargeCode/${selectedChargeCodeId}`, {
            method: "PUT"
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert("Charge Code marked as Complete!", "success");
                fetchCodeDatas(); 
            } else {
                showAlert("Error: " + data.message, "danger");
            }
        })
        .catch(error => {
            console.error("Error updating charge code status:", error);
            showAlert("An error occurred while completing the charge code.", "danger");
        })
        .finally(() => {
            var completeModal = bootstrap.Modal.getInstance(document.getElementById("completeModal"));
            completeModal.hide();
        });
    }
});



let selectedEmployees = [];

function openAssignModal(codeId, chargeCode, description) {
    selectedEmployees = [];
    document.getElementById("selectedEmployeesContainer").innerHTML = "";

    document.getElementById("chargeCodeDisplay").innerText = chargeCode;
    document.getElementById("chargeCodeDescription").innerText = description;

    
    document.getElementById("employeeDropdown").addEventListener("click", () => {
        
        document.getElementById("employeeSearch").value = "";
        
        let employees = document.querySelectorAll("#employeeList li");
        employees.forEach(emp => {
            emp.style.display = "block";
        });
    });

    const modal = new bootstrap.Modal(document.getElementById("assignModal"));
    modal.show();
}

function filterEmployees() {
    let searchValue = document.getElementById("employeeSearch").value.toLowerCase();
    let employees = document.querySelectorAll("#employeeList li a");

    employees.forEach(employee => {
        let text = employee.innerText.toLowerCase();
        employee.parentElement.style.display = text.includes(searchValue) ? "block" : "none";
    });
}

function addEmployeetofield(name, role) {
    if (!selectedEmployees.includes(name)) {
        selectedEmployees.push(name);
        updateSelectedEmployees();
    }
}

function updateSelectedEmployees() {
    let container = document.getElementById("selectedEmployeesContainer");
    container.innerHTML = "";

    selectedEmployees.forEach(employee => {
        let employeeDiv = document.createElement("div");
        employeeDiv.className = "d-flex align-items-center justify-content-between bg-light p-2 rounded mb-1";

        employeeDiv.innerHTML = `
            <span>${employee}</span>
            <button class="btn btn-danger btn-sm" onclick="removeEmployee('${employee}')">&times;</button>
        `;

        container.appendChild(employeeDiv);
    });
}

function removeEmployee(employeeName) {
    selectedEmployees = selectedEmployees.filter(emp => emp !== employeeName);
    updateSelectedEmployees();
}


function assignEmployees() {
	
    if (selectedEmployees.length === 0) {
        showAlert("Please select at least one employee.", "danger");
        return;
    }

	
    
    const chargeCode = document.getElementById("chargeCodeDisplay").innerText;
    const description = document.getElementById("chargeCodeDescription").innerText;

  
    document.getElementById("confirmationChargeCode").innerText = chargeCode;
    document.getElementById("confirmationDescription").innerText = description;


    const confirmationModal = new bootstrap.Modal(document.getElementById('confirmationModal2'));
    confirmationModal.show();

  
    document.getElementById("confirmAssignment").onclick = function() {
		
		showLoader();
        const assignmentData = {
            chargeCode: chargeCode,
            description: description,
            employees: selectedEmployees  
			
        };

		
     
        fetch("/assignEmployees", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(assignmentData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert("Employees Assigned and Email Sent!", "success");
            } else {
                showAlert("Error in assignment: " + data.message, "danger");
            }
        })
        .catch(error => {
            console.error("Error sending data to backend:", error);
            showAlert("An error occurred.", "danger");
        })

		.finally(() => {
		           hideLoader(); 
		           confirmationModal.hide(); 
		           bootstrap.Modal.getInstance(document.getElementById("assignModal")).hide(); 
		       });
    };


    document.querySelector('.btn-secondary').onclick = function() {
        confirmationModal.hide();
    };
}




/*Fetch Expense code*/
function fetchExpense() {
    fetch("/getExpensecode") 
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("Expense-table-body");
            tableBody.innerHTML = ""; 
            data.forEach(ExCode => {
                tableBody.innerHTML += `
				<tr>
				    
				    <td>${ExCode["Ex-code"]}</td>  <!-- Use correct JSON key -->
				     <td>${ExCode["Ex-type"]}</td>  <!-- Use correct JSON key -->
				      <td>
				      <button class="btn btn-success btn-sm" onclick="editExpense('${ExCode.id}')"><i class="bi bi-pencil-square"></i></button>
					  <button class="btn btn-danger btn-sm" onclick="DeleteExpense('${ExCode.id}')"><i class="bi bi-trash3"></i></button>
				   </td>
				                 
                `;
            });
			paginateTable("Expense-table-body");
        })
        .catch(error => console.error("Error fetching Expense Details:", error));
}

function DeleteExpense(ExpId) {

    expesetoDelete = ExpId; 
    const deleteModal = new bootstrap.Modal(document.getElementById('ExpdeleteModal'));
    deleteModal.show();
}


document.getElementById("confirmExpDeleteBtn").addEventListener("click", function () {
    if (expesetoDelete) {
        fetch(`/deleteExpense/${expesetoDelete}`, {
            method: "DELETE",
        })
        .then(response => response.text())
        .then(message => {
            showAlert(message, "success");
            fetchExpense();
        })
        .catch(error => console.error("Error deleting charge code:", error))
        .finally(() => {
            expesetoDelete = null;
            const modal = bootstrap.Modal.getInstance(document.getElementById('ExpdeleteModal'));
            modal.hide();
        });
    }
});

function editExpense(exId) {
    fetch(`/getExpenseById/${exId}`)
        .then(response => response.json())
        .then(data => {


            const editFormHTML = `               
				<div class="card p-3 mb-3">
					        <h4>Edit Expense Details</h4>
					        <form onsubmit="submitUpdatedExpense(event, '${data.id}')">
					            ${inputField("Expense Code", "text", "Ex-code", "", data["Ex-code"])}
								${selectField("Expense Name", "Ex-type", 
									["Select", "Travel","Accommodation", "Meals & Entertainment","Communication","Office Supplies","Subscription/Software","IT Equipment","Training & Certification","Miscellaneous "], data["Ex-type"])}
					            ${formButtons()}
					        </form>
					   </div>
            `;
            document.getElementById("form-container").innerHTML = editFormHTML;
        })
        .catch(error => console.error("Error fetching employee for edit:", error));
}

function submitUpdatedExpense(event, id) {
    event.preventDefault();
    showLoader(); 

    const updatedData = {
        id: id,
        "Ex-code": document.getElementById("Ex-code").value,
        "Ex-type": document.getElementById("Ex-type").value,
    };

    fetch("/updateExpense", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedData),
    })
    .then(response => response.text())
    .then(message => {
        hideLoader(); 
        hideForm();
        showAlert(message, "success");
        fetchExpense();
    })
    .catch(error => {
        hideLoader(); 
        console.error("Error updating employee:", error);
    });
}

/*Fetch delegator details*/
function fetchDelegator() {
    fetch("/getDelegator") 
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("Delegate-table-body");
            tableBody.innerHTML = ""; 
            data.forEach(Delegator => {
                tableBody.innerHTML += `
				<tr>
				    <td>${Delegator.id}</td>
				    <td>${Delegator["D-name"]}</td>  <!-- Use correct JSON key -->
				     <td>${Delegator["SA-email"]}</td>  <!-- Use correct JSON key -->
				      <td>
				      <button class="btn btn-success btn-sm" onclick="editDelegatores('${Delegator.id}')"><i class="bi bi-pencil-square"></i></button>
				   </td>
				                 
                `;
            });
			paginateTable("Delegate-table-body");
        })
        .catch(error => console.error("Error fetching Delegator Details:", error));
}



function fetchHoliday() {
    fetch("/getHolidays") 
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("holiday-table-body");
            tableBody.innerHTML = ""; 
			data.forEach(leave => {
			    // Format holiday date			   				

			    tableBody.innerHTML += `
			        <tr>
			            <td>${leave["holidayname"]}</td>
			            <td>${leave["holidaydate"]}</td>
			            <td>${leave.year}</td>
			            <td>
			                <button class="btn btn-success btn-sm" onclick="editholiday('${leave.id}')"><i class="bi bi-pencil-square"></i></button>
			                <button class="btn btn-danger btn-sm" onclick="Deleteholiday('${leave.id}')"><i class="bi bi-trash3"></i></button>
			            </td>
			        </tr>
			    `;
			});
			paginateTable("holiday-table-body");
        })
        .catch(error => console.error("Error fetching Expense Details:", error));
}

function Deleteholiday(ExpId) {

    holidaydelete = ExpId; 
    const deleteModal = new bootstrap.Modal(document.getElementById('holidaymodal'));
    deleteModal.show();
}


document.getElementById("confirmHolidayDeleteBtn").addEventListener("click", function () {
    if (holidaydelete) {
        fetch(`/deleteholiday/${holidaydelete}`, {
            method: "DELETE",
        })
        .then(response => response.text())
        .then(message => {
            showAlert(message, "success");
            fetchHoliday();	
        })
        .catch(error => console.error("Error deleting holiday:", error))
        .finally(() => {
            expesetoDelete = null;
            const modal = bootstrap.Modal.getInstance(document.getElementById('holidaymodal'));
            modal.hide();
        });
    }
});
function editholiday(Id) {
    fetch(`/getholidaybyid/${Id}`)
        .then(response => response.json())
		.then(data => {
		    const originalDate = data["holidaydate"]; // e.g., "01/04/2025"
		    let dateParts = originalDate.split("/"); // [ "01", "04", "2025" ]
		    let formattedDateForInput = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}`; // "2025-04-01"

		    const editFormHTML = `               
		        <div class="card p-3 mb-3">
		            <h4>Edit Holiday</h4>
		             <form onsubmit="SubmitUpdatedHoliday(event, '${data.id}')">
		                ${inputField("Holiday Name", "text", "holidayname", "", data["holidayname"])}
		                ${inputField("Date", "date", "holidaydate", "", formattedDateForInput)}
		                ${formButtons()}
		            </form>
		        </div>
		    `;
		    document.getElementById("form-container").innerHTML = editFormHTML;
		})
        .catch(error => console.error("Error fetching employee for edit:", error));
}

function SubmitUpdatedHoliday(event, id) {
    event.preventDefault();
    showLoader(); 

    const updatedData = {
        id: id,
        "holidayname": document.getElementById("holidayname").value,
        "holidaydate": document.getElementById("holidaydate").value,
    };

    fetch("/updateHoliday", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedData),
    })
    .then(response => response.text())
    .then(message => {
        hideLoader(); 
        hideForm();
        showAlert(message, "success");
       fetchHoliday();
    })
    .catch(error => {
        hideLoader(); 
        console.error("Error updating employee:", error);
    });
}






/*Form creation for all the options*/
function createForm(type) {
    const forms = {
        "employee": ` 
            <div class="card p-3 mb-3">
                <h4>Add Employee</h4>
                <form action="/addEmployee" method="POST" autocomplete="off">
                    ${inputField("Name", "text", "E-name")}
					${inputField("Email", "email", "E-mail")}
					${inputField("Password", "password", "E-pass", "", "", true)}
					${inputField("Onboard date", "date", "onborad")}
					${inputField("Designation", "text", "E-desg")}
                    ${selectField("Role", "E-role", ["Select Role","Admin","Employee"])}
                    ${formButtons()}
                </form>
            </div>
        `,
        "delegates": `
		<div class="card p-3 mb-3">
		                <h4>Add Delegate</h4>
						<form action="/addDelegate" method="POST">
						<div class="mb-3">
						               <label class="form-label">Delegates Name</label>
						               <select id="delegateName" name="D-name" class="form-control" onchange="updateDelegateEmail()" required>
						                   <option value="">Select Employee</option>
						               </select>
						           </div>

						           <div class="mb-3">
						               <label class="form-label">Email</label>
						               <input type="email" id="delegateEmail" name="SA-email" class="form-control" required >
						           </div>
						           
						            ${formButtons()}
						        </form>
		            </div>
        `,
        "charge-code": `
            <div class="card p-3 mb-3">
                <h4>Add Charge Code</h4>
                <form action="/addChargeCode" method="POST">  
				${selectField("Code Type", "codeType", ["Charge code"])}        
				${selectField("Project Type", "projectType", ["External","Internal"])}
				${inputField("Client/Organization", "text", "clientName")}
				${inputField("Onboard/Start date", "date", "startDate")}
				${inputField("Country/Region", "text", "country")}
				${textareaField("Description", "description")}
				${inputField("Charge Code", "text", "code", "charge-code")}
				${formButtons()}
                </form>
            </div>
        `,
        "leave-code": `
            <div class="card p-3 mb-3">
                <h4>Add Leave Code</h4>
                <form action="/addChargeCode" method="POST">
				    ${selectField("Code Type", "codeType", ["Leave code"])} 
                    ${inputField("Leave Code", "text", "code", "leave-code")}
					${inputField("Leave Name", "text", "description")}
                    ${formButtons()}
                </form>
            </div>
        `
		,
		"Expense-code": `
		   <div class="card p-3 mb-3">
		        <h4>Add Expense Code</h4>
		        <form action="/addExpenseCode" method="POST">
		            ${inputField("Expense Code", "text", "Ex-code")}
					${selectField("Expense Name", "Ex-type", 
						["Select", "Travel","Accommodation", "Meals & Entertainment","Communication","Office Supplies","Subscription/Software","IT Equipment","Training & Certification","Miscellaneous "])}
		            ${formButtons()}
		        </form>
		   </div>
		`,
		"Holiday": `
				   <div class="card p-3 mb-3">
				        <h4>Add Holiday</h4>
				        <form action="/addHoliday" method="POST">
				            ${inputField("Holiday Name", "text", "holidayname")}
							${inputField("Date", "date", "holidaydate")}
				            ${formButtons()}
				        </form>
				   </div>
				`,

				"addSalary": `
					<div class="card p-3 mb-3">
						<h4>Add Salary for Employee</h4>
						    <form action="/addSalary" method="POST" autocomplete="off">
								<div class="mb-3">
								    <label class="form-label">Employee Name</label>
								      <select class="form-control" name="E-name" id="salaryEmployeeSelect" onchange="updateDOJ()" required>
								              <option value="">Select Employee</option>
								        </select>
								    </div>
								${inputField("DOJ", "text", "doj", "", "", true, )} 
								${inputField("Salary(Month)", "number", "Salary_M", "", "", false, "min='1000'")}
								${inputField("Effective From", "date", "effective")}
								${formButtons()}
							</form>
						</div>
					`
    };
	setTimeout(fetchEmfordeg, 0);

    return forms[type] || "<p>Form Not Found</p>";
}


/* Function for fetching User detials in the db for making delegate*/
function fetchEmfordeg() {
	fetch("/getEmployeedata")
	       .then(response => response.json())
	       .then(data => {
	           const dropdown = document.getElementById("delegateName");
	           if (!dropdown) return;

	           dropdown.innerHTML = '<option value="">Select Employee</option>'; 

	           data.forEach(employee => {
	               let option = document.createElement("option");
	               option.value = employee.name;  
	               option.textContent = employee.name; 
	               option.dataset.email = employee.email; 
	               dropdown.appendChild(option);
	           });
	       })
	       .catch(error => console.error("Error fetching employee data:", error));
	}
	
function updateDelegateEmail() {
	    const dropdown = document.getElementById("delegateName");
	    const emailField = document.getElementById("delegateEmail");

	    const selectedOption = dropdown.options[dropdown.selectedIndex]; 
	    emailField.value = selectedOption.dataset.email || "";
	}

	
/*External charge code generator*/
function codeGenerate() {
	    const clientNameInput = document.getElementById("clientName");
	    const onboardDateInput = document.getElementById("startDate");
	    const chargeCodeInput = document.getElementById("code");
	    
	    if (!clientNameInput || !onboardDateInput || !chargeCodeInput) {
	        console.error("Missing input fields for Charge Code generation.");
	        return;
	    }
	    
	    if (chargeCodeInput.value) {
	        return; 
	    }
	    
	    const clientName = clientNameInput.value.trim().replace(/\s+/g, "").toUpperCase();
	    const onboardDate = onboardDateInput.value.replace(/-/g, "");
	    
	    if (!clientName || !onboardDate) {
	        showAlert("Please enter both Client Name and Onboard Date to generate the Charge Code.", "danger");
	        return;
	    }
	    

	    getNextCodeIncrement(clientName, onboardDate, chargeCodeInput);
	}

	function getNextCodeIncrement(clientName, onboardDate, chargeCodeInput) {
	    fetch(`/getNextCodeIncrement`)
	        .then(response => response.json())
	        .then(data => {
	            const lastIncrement = data;
	            const incrementedValue = String(lastIncrement).padStart(3, '0');
	            const generatedCode = `${clientName}${onboardDate}td${incrementedValue}`;
	            
	            chargeCodeInput.value = generatedCode;
	            chargeCodeInput.setAttribute("readonly", true);
	            chargeCodeInput.onkeydown = function(event) { event.preventDefault(); };
	        })
	        .catch(error => {
	            console.error("Error generating charge code:", error);
	        });
	}


	/*Temp password Generator*/
function generatePassword(inputId) {
    const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    let randomPassword = "";
    for (let i = 0; i < 8; i++) {
        randomPassword += chars.charAt(Math.floor(Math.random() * chars.length));
    }
	const inputField = document.getElementById(inputId);
	    inputField.value = randomPassword;
	    inputField.setAttribute("readonly", true); 
	    inputField.onkeydown = function(event) { event.preventDefault(); };
}

/*Input field function*/
function inputField(label, type, name, formType = "", value = "", readonly = false, extraAttrs = "") {
    const isPasswordField = name === "E-pass" || name === "SA-pass";
    const isChargeCodeField = name === "code" && formType === "charge-code";

    return `
        <div class="mb-3">
            <label class="form-label">${label}</label>
            <input 
                type="${type}" 
                class="form-control" 
                name="${name}" 
                id="${name}" 
                value="${value || ""}" 
                ${readonly ? "readonly" : ""} 
                ${extraAttrs} 
                required
            >
            ${isPasswordField ? `<button class="btn btn-outline-primary" type="button" onclick="generatePassword('${name}')" style="margin-top: 10px;">Generate</button>` : ""}
            ${isChargeCodeField ? `<button class="btn btn-outline-primary" type="button" onclick="codeGenerate()" style="margin-top: 10px;">Generate Charge Code</button>` : ""}
        </div>
    `;
}





/*Select field function*/
function selectField(label, name, options, selectedValue = "") {
    return `
        <div class="mb-3">
            <label class="form-label">${label}</label>
            <select class="form-control" name="${name}" id="${name}">
                ${options.map(opt => `<option value="${opt}" ${opt === selectedValue ? "selected" : ""}>${opt}</option>`).join('')}
            </select>
        </div>
    `;
}

function textareaField(label, name, value = "") {
    return `
        <div class="mb-3">
            <label class="form-label">${label}</label>
            <textarea class="form-control" name="${name}" id="${name}" required>${value}</textarea>
        </div>
    `;
}



/*function for buttons*/
function formButtons() {
    return `
        <button class="btn btn-success" type="submit">Save</button>
        <button class="btn btn-secondary" type="button" onclick="hideForm()">Cancel</button>
    `;
}



function hideForm() {
    document.getElementById("form-container").innerHTML = "";
}

function closeSidebarOnMobile() {
    if (window.innerWidth <= 768) {
        document.getElementById("sidebar").classList.remove("show");
    }
}


document.querySelectorAll(".nav-link").forEach(link => {
    link.addEventListener("click", function () {
        closeSidebarOnMobile();
    });
});
	