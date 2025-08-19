// SalaryLogic.js
function paginateTable(tableId, rowsPerPage = 10) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const rows = table.querySelectorAll("tbody tr");
    const totalPages = Math.ceil(rows.length / rowsPerPage);
    const paginationContainerId = `${tableId}-pagination`;


    let existingContainer = document.getElementById(paginationContainerId);
    if (existingContainer) existingContainer.remove();


    if (rows.length <= rowsPerPage) return;


    let paginationContainer = document.createElement("div");
    paginationContainer.id = paginationContainerId;
    paginationContainer.classList.add("pagination-container");
    table.parentElement.appendChild(paginationContainer);

    function showPage(page) {
        const start = (page - 1) * rowsPerPage;
        const end = start + rowsPerPage;

        rows.forEach((row, index) => {
            row.style.display = (index >= start && index < end) ? "" : "none";
        });

        updatePagination(page);
    }

    function updatePagination(activePage) {
        paginationContainer.innerHTML = "";

        const prev = document.createElement("span");
        prev.innerHTML = "&lt;";
        prev.className = "pagination-arrow";
        prev.onclick = () => showPage(Math.max(1, activePage - 1));
        paginationContainer.appendChild(prev);

        for (let i = 1; i <= totalPages; i++) {
            const pageBtn = document.createElement("span");
            pageBtn.textContent = i;
            pageBtn.className = `pagination-dot ${i === activePage ? "active" : ""}`;
            pageBtn.onclick = () => showPage(i);
            paginationContainer.appendChild(pageBtn);
        }

        const next = document.createElement("span");
        next.innerHTML = "&gt;";
        next.className = "pagination-arrow";
        next.onclick = () => showPage(Math.min(totalPages, activePage + 1));
        paginationContainer.appendChild(next);
    }

    // 👇 Initially show first page
    showPage(1);
}

function fetchEmployeesForSalaryForm() {
    fetch("/getEmployeesforSalary")
        .then(response => response.json())
        .then(data => {
			console.log(data)
            const select = document.getElementById("salaryEmployeeSelect");
            if (!select) return;

            select.innerHTML = '<option value="">Select Employee</option>';

            data.forEach(emp => {
                const option = document.createElement("option");
                option.value = emp["E-name"];
                option.textContent = emp["E-name"];
                option.dataset.doj = emp["onborad"];
                select.appendChild(option);
            });
        })
        .catch(error => console.error("Error fetching employees for salary:", error));
}

function updateDOJ() {
    const select = document.getElementById("salaryEmployeeSelect");
    const dojInput = document.getElementById("doj");

    const selectedOption = select.options[select.selectedIndex];
    dojInput.value = selectedOption.dataset.doj || "";
}


//Employee
function openBankEditModal() {
  document.getElementById('inputAccountHolder').value = document.getElementById('bankAccountHolder').textContent;
  document.getElementById('inputAccountNumber').value = document.getElementById('fullAccountNumber').value;
  document.getElementById('inputConfirmAccountNumber').value = document.getElementById('fullAccountNumber').value;
  document.getElementById('inputIFSC').value = document.getElementById('bankIFSC').textContent;
  document.getElementById('inputBankName').value = document.getElementById('bankName').textContent;

  // Set bank book preview if exists
  const bankBookImgSrc = document.getElementById('bankBookPhoto').getAttribute('src');
  const inputPreview = document.getElementById('inputBankBookPreview');
  if (bankBookImgSrc && bankBookImgSrc !== '#') {
    inputPreview.src = bankBookImgSrc;
    inputPreview.style.display = 'block';
  } else {
    inputPreview.style.display = 'none';
  }

  const modal = new bootstrap.Modal(document.getElementById('bankEditModal'));
  modal.show();
}

function saveBankDetails() {
  const username = sessionStorage.getItem("userName");
  const accountHolder = document.getElementById('inputAccountHolder').value.trim();
  const accountNumber = document.getElementById('inputAccountNumber').value.trim();
  const confirmAccountNumber = document.getElementById('inputConfirmAccountNumber').value.trim();
  const ifsc = document.getElementById('inputIFSC').value.trim().toUpperCase();
  const bankName = document.getElementById('inputBankName').value.trim();
  const bankBookFile = document.getElementById('inputBankBook').files[0]; // Get file

  // Clear previous errors
  const errorFields = [
    'errorAccountHolder',
    'errorAccountNumber',
    'errorConfirmAccountNumber',
    'errorIFSC',
    'errorBankName',
    'errorBankBook'
  ];
  errorFields.forEach(id => document.getElementById(id).innerText = "");

  let hasError = false;

  // Validate fields
  if (!accountHolder) {
    document.getElementById("errorAccountHolder").innerText = "Please enter account holder name.";
    hasError = true;
  }

  if (!accountNumber) {
    document.getElementById("errorAccountNumber").innerText = "Please enter account number.";
    hasError = true;
  } else if (!/^\d{9,18}$/.test(accountNumber)) {
    document.getElementById("errorAccountNumber").innerText = "Account number must be 9 to 18 digits.";
    hasError = true;
  }

  if (!confirmAccountNumber) {
    document.getElementById("errorConfirmAccountNumber").innerText = "Please confirm the account number.";
    hasError = true;
  } else if (accountNumber !== confirmAccountNumber) {
    document.getElementById("errorConfirmAccountNumber").innerText = "Account numbers do not match.";
    hasError = true;
  }

  if (!ifsc) {
    document.getElementById("errorIFSC").innerText = "Please enter IFSC code.";
    hasError = true;
  } else if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifsc)) {
    document.getElementById("errorIFSC").innerText = "Invalid IFSC format (e.g., SBIN0001234).";
    hasError = true;
  }

  if (!bankName) {
    document.getElementById("errorBankName").innerText = "Please enter bank name.";
    hasError = true;
  }

  // Optional: validate image type/size
  if (bankBookFile) {
    const validTypes = ["image/jpeg", "image/png", "image/jpg"];
    if (!validTypes.includes(bankBookFile.type)) {
      document.getElementById("errorBankBook").innerText = "Bank book photo must be JPG or PNG.";
      hasError = true;
    }
  }

  if (hasError) return;

  // Confirmation modal
  document.getElementById("confirmBankText").innerText =
    `Are you sure you want to save the bank details for ${accountHolder}?`;
  document.getElementById("bankWarningNote").innerText =
    "Note: If you enter wrong details, it may affect the salary process.";

  bootstrap.Modal.getInstance(document.getElementById("bankEditModal")).hide();
  const confirmModal = new bootstrap.Modal(document.getElementById("confirmBankModal"));
  confirmModal.show();

  document.getElementById("confirmSaveBank").onclick = function () {
    let formData = new FormData();
    formData.append("Employeename", username);
    formData.append("accountHolder", accountHolder);
    formData.append("accountNumber", accountNumber);
    formData.append("ifsc", ifsc);
    formData.append("bankName", bankName);
    if (bankBookFile) {
      formData.append("bankBookPhoto", bankBookFile);
    }

    fetch("/saveBankDetails", {
      method: "POST",
      body: formData,
      headers: {
        "Accept": "application/json",
      }
    })
      .then(response => response.json())
      .then(data => {
        showAlert(data.message, "success");
        document.getElementById("bankEditForm").reset();
        bootstrap.Modal.getInstance(document.getElementById("bankEditModal")).hide();
        fetchBankDetails();
      })
      .catch(error => {
        console.error("Error:", error);
        showAlert(error.message || "Error saving bank details!", "danger");
      })
      .finally(() => {
        confirmModal.hide();
      });
  };
}



function validateAccountNumber(inputId, errorId) {
    const input = document.getElementById(inputId);
    const errorDiv = document.getElementById(errorId);
    const value = input.value;

    // Allow only digits
    if (!/^\d*$/.test(value)) {
        errorDiv.textContent = "Only numeric digits are allowed.";
    } else {
        errorDiv.textContent = "";
  }
  }

  
  function openPhotoModal() {
    const modal = new bootstrap.Modal(document.getElementById("bankPhotoModal"));
    modal.show();
  }

  function fetchBankDetails() {
    const username = sessionStorage.getItem("userName");
    if (!username) {
      console.error("Username not found in session");
      return;
    }

    fetch(`/getBankDetails?Employeename=${encodeURIComponent(username)}`)
      .then(response => response.json())
      .then(data => {
        document.getElementById("bankAccountHolder").innerText = data.accountHolder || "-";

        let maskedAccountNumber = "-";
        if (data.accountNumber && data.accountNumber.length >= 6) {
          const lastSix = data.accountNumber.slice(-6);
          const masked = "X".repeat(data.accountNumber.length - 6) + lastSix;
          maskedAccountNumber = masked;
        }
        document.getElementById("bankAccountNumber").innerText = maskedAccountNumber;
        document.getElementById("bankIFSC").innerText = data.ifsc || "-";
        document.getElementById("bankName").innerText = data.bankName || "-";

        // Save real account number
        document.getElementById("fullAccountNumber").value = data.accountNumber || "";

        // Load and show bank book photo if available
        const bankPhoto = document.getElementById("bankBookPhoto");
        if (data.bankBookPhotoPath) {
			const fullImageUrl = data.bankBookPhotoPath.replace(/^[:]?/, '').replace(/\\/g, '/');
          bankPhoto.src = fullImageUrl;
          bankPhoto.style.display = "block";

          // Set modal image src
          document.getElementById("bankPhotoModalImg").src = fullImageUrl;
        } else {
          bankPhoto.style.display = "none";
        }
      })
      .catch(error => {
        console.error("Error fetching bank details:", error);
      });
  }




document.addEventListener("DOMContentLoaded", function () {
	    const navLinks = document.querySelectorAll(".nav-link");

	    navLinks.forEach(link => {
	        link.addEventListener("click", function () {
	         
	            navLinks.forEach(nav => nav.classList.remove("active"));
	            
	            // Add active class to clicked link
	            this.classList.add("active");
	        });
	    });
	});

	
//Admin payslip generation
function loadEmployeesForMonth() {
  const monthInput = document.getElementById('monthPicker');
  const selectedMonth = monthInput.value; // format: YYYY-MM

  fetch(`/payslip/EmployePayslip/${selectedMonth}`)
    .then(response => response.text())  // get raw response as text first
    .then(text => {
      const data = JSON.parse(text);   // parse JSON manually

   

      const employeeSelect = document.getElementById('employeeSelect');
      employeeSelect.innerHTML = '<option value="">Select Employee</option>';
      data.forEach(emp => {
        const option = document.createElement('option');
        option.value = emp.username;
        option.text = emp.display;
        employeeSelect.appendChild(option);
      });
    })
    .catch(err => {
      console.error('Error loading employees:', err);
    });

}


async function loadPayslipDetails() {
    const month     = document.getElementById("monthPicker").value;
    const username  = document.getElementById("employeeSelect").value;

    if (!month || !username) {
        showAlert("Please select both month and employee.", "danger");
        return;
    }

    const url = `/payslip/details?username=${encodeURIComponent(username)}&month=${month}`;

    try {
        const response = await fetch(url);

        /*  ── try to read JSON even if status ≠ 200 ── */
        let payload = {};
        try { payload = await response.json(); } catch { /* ignore parse errors */ }

        if (!response.ok) {                        // 4xx / 5xx
            const msg = payload.error              // backend supplied?
                       || `Server error ${response.status}`;
            showAlert(msg, "danger");
            throw new Error(msg);                  // still hit catch for console
        }

        if (payload.error) {                       // backend’s own “error” key
            showAlert(payload.error, "danger");
            return;
        }

        /*  ── build preview exactly as before ── */
        const formattedMonth = new Date(month + "-01").toLocaleString('default',
                               { month: 'long', year: 'numeric' });

        const payslipHTML = `
            <div class="card shadow-sm p-4 mb-4">
              <h4 class="mb-3">Payslip Preview – ${formattedMonth}</h4>
              <table class="table table-bordered">
                <tbody>
                  <tr><th>Employee Name</th><td>${payload.name}</td></tr>
                  <tr><th>Onboarded Date</th><td>${payload.onboardDate}</td></tr>
                  <tr><th>Designation</th><td>${payload.designation}</td></tr>
                  <tr><th>Basic Salary</th><td>₹${(+payload.basicSalary).toLocaleString()}</td></tr>
                  <tr><th>STD Days</th><td>${payload.stddays}</td></tr>
                  <tr><th>Worked Days</th><td>${payload.totalworked}</td></tr>
                  <tr><th>Paid Leaves</th><td>${payload.totalleaves - payload.lop}</td></tr>
                  <tr><th>Loss of Pay</th><td>${payload.lop}</td></tr>
                  <tr><th>Deductions</th><td>₹${(+payload.deduction).toLocaleString()}</td></tr>
                  <tr class="table-success"><th>Net Pay</th>
                      <td><strong>₹${(+payload.netPay).toLocaleString()}</strong></td></tr>
                </tbody>
              </table>
            </div>`;
        document.getElementById("payslip-preview-container").innerHTML = payslipHTML;

    } catch (err) {
        console.error("Error fetching payslip details →", err);
        /* showAlert already called above for non‑OK responses */
    }
}




function showPayslipApproveConfirmation() {
    const month = document.getElementById("monthPicker").value;
    const username = document.getElementById("employeeSelect").value;

    if (!month || !username) {
        showAlert("Please select both month and employee before approving.", "danger");
        return;
    }

    // Format confirmation message
    const formattedMonth = new Date(month + "-01").toLocaleString("default", { month: "long", year: "numeric" });
	document.getElementById("confirmMessage").innerHTML =
	    `Are you sure you want to approve <strong class="text-primary">${username}</strong>'s payslip for salary processing for the month of <strong class="text-success">${formattedMonth}</strong>?`;

    // Show modal (Bootstrap 5)
    const modal = new bootstrap.Modal(document.getElementById("payslipapproveConfirmModal"));
    modal.show();

    // Attach approve handler (once)
    document.getElementById("confirmApproveBtn").onclick = () => {
        modal.hide();
        appreovPaylisp(); // Your original function
    };
}

async function appreovPaylisp() {
    try {
		showLoader();
        // Get selected month and employee
        const month = document.getElementById("monthPicker").value;
        const username = document.getElementById("employeeSelect").value;

        if (!month || !username) {
            showAlert("Please select both month and employee before approving.", "danger");
            return;
        }

        // Get payslip table rows
        const rows = document.querySelectorAll("#payslip-preview-container table tbody tr");

     
		const employeeName = rows[0].cells[1].innerText;
		const onboardDate = rows[1].cells[1].innerText;
		const designation = rows[2].cells[1].innerText;
		const basicSalary = rows[3].cells[1].innerText.replace(/₹|,/g, "");
		const stdWorkDays = rows[4].cells[1].innerText;
		const totalWorkingDays = rows[5].cells[1].innerText;
		const totalLeaves = rows[6].cells[1].innerText;
		const lop = rows[7].cells[1].innerText;
		const deductions = rows[8].cells[1].innerText.replace(/₹|,/g, "");
		const netPay = rows[9].cells[1].innerText.replace(/₹|,/g, "");
		
		console.log(basicSalary);
		console.log(stdWorkDays);

    
        const payload = {
            username: employeeName,
            month: month,
            onboardDate: onboardDate,
            designation: designation,
			basicSalary: parseFloat(basicSalary),
            stdWorkDays: parseInt(stdWorkDays),
			totalWorkingDays: parseInt(totalWorkingDays),
            totalLeaves: parseInt(totalLeaves),
            lop: parseFloat(lop),          
            deductions: parseFloat(deductions),
            netPay: parseFloat(netPay),
            
        };

    
        const response = await fetch('/payslip/approvepayslip', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const result = await response.json();

        if (response.ok) {
			loadEmployeesForMonth();
            showAlert("Payslip approved and saved successfully!", "success");
			  document.getElementById("payslip-preview-container").innerHTML = `
			      <div class="card shadow-sm p-4 mb-4">
			        <h4 class="mb-3">Payslip Preview –</h4>
			        <table class="table table-bordered">
			          <tbody>
			            <tr><th>Employee Name</th><td>-</td></tr>
			            <tr><th>Onboarded Date</th><td>-</td></tr>
			            <tr><th>Designation</th><td>-</td></tr>
			            <tr><th>STD Work days</th><td>-</td></tr>
			            <tr><th>Total Leaves</th><td>-</td></tr>
			            <tr><th>Total Working Days</th><td>-</td></tr>
			            <tr><th>LOP</th><td>-</td></tr>
			            <tr><th>Basic Salary</th><td>₹</td></tr>
			            <tr><th>Deductions</th><td>₹</td></tr>
			            <tr class="table-success"><th>Net Pay</th><td><strong>₹</strong></td></tr>
			          </tbody>
			        </table>
			      </div>
			    `;

			    // Optionally reset the selects too:
			    document.getElementById("employeeSelect").innerHTML = '<option value="">Select Employee</option>';
				fetchPayslipData();
				
			}
			
         else {
            showAlert("Failed to approve payslip: " + (result.error || "Unknown error"), "danger");
        }

    } catch (error) {
        console.error("Error approving payslip:", error);
        showAlert("Error approving payslip. Check console for details.", "danger");
		} finally {
		        hideLoader(); 
		    }
}



function fetchPayslipData() {
    fetch("/payslip/getPayslipdata")
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById("payslip-table-body");
            tableBody.innerHTML = "";

			const formatDateTime = (datetimeStr) => {
			  if (!datetimeStr || datetimeStr === "-") return datetimeStr;

			  const fixedStr = datetimeStr.replace(":", " ");

			  const dateObj = new Date(fixedStr);
			  if (isNaN(dateObj)) return datetimeStr;

			  const day = String(dateObj.getDate()).padStart(2, '0');
			  const month = dateObj.toLocaleString('default', { month: 'short' });
			  const year = dateObj.getFullYear();

			  const timeFormatted = dateObj.toLocaleTimeString('en-US', {
			    hour: '2-digit',
			    minute: '2-digit',
			    hour12: true
			  });

			  return `${day}-${month}-${year} | ${timeFormatted}`;
			};

			
            data.forEach(payslip => {
                const [year, month] = payslip.month.split("-");
                const displayMonth = new Date(year, month - 1).toLocaleString('default', { month: 'short' }) + " - " + year;
                const monthParam = `${year}-${month.padStart(2, '0')}`;

                const formattedBasicSalary = "₹ " + Number(payslip.basicSalary).toLocaleString('en-IN', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                });

                const formattedNetPay = "₹ " + Number(payslip.netPay).toLocaleString('en-IN', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                });

                tableBody.innerHTML += `
                    <tr>
                        <td>${payslip.id}</td>
                        <td>${payslip.username}</td> 
                        <td>${payslip.designation}</td> 
                        <td>${formattedBasicSalary}</td> 
                        <td>${displayMonth}</td>
                        <td>${payslip.stdWorkDays}</td>		
                        <td>${payslip.totalWorkingDays}</td>	
                        <td>${formattedNetPay}</td>	
                        <td>${payslip.bankName}</td>
						<td>${formatDateTime(payslip.salaryProcessAt)}</td>
                        <td>
                            <button class="btn btn-sm btn-primary" onclick="downloadPayslipAdmin('${payslip.username}', '${monthParam}')">
                                View PDF
                            </button>
                        </td>				                       
                    </tr>
                `;
            });
			paginateTable("payslip-table-body");
        })
        .catch(error => console.error("Error fetching Payslip data:", error));
}


function downloadPayslipAdmin(username, month) {
    fetch(`/PayslipDownload?username=${encodeURIComponent(username)}&month=${encodeURIComponent(month)}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to download PDF");
            return response.blob();
        })
        .then(blob => {
            const url = URL.createObjectURL(blob);
            window.open(url, '_blank'); // 👉 opens in new tab
        })
        .catch(err => {
            console.error("Error downloading PDF:", err);
            showAlert("Failed to open payslip.", "danger");
        });
}



function downloadPayslip() {
    const username = sessionStorage.getItem("userName");

    function getSelectedPeriod() {
        return periodDropdown.options[periodDropdown.selectedIndex].text; // "01/05/2025 - 15/05/2025"
    }

    const extractedPeriod = getSelectedPeriod();

    // Extract "2025-05" from "01/05/2025 - 15/05/2025"
    const dateParts = extractedPeriod.split(" - ")[0].split("/"); // ["01", "05", "2025"]
    const formattedMonth = `${dateParts[2]}-${dateParts[1]}`;       // "2025-05"

    fetch(`/PayslipDownload?username=${encodeURIComponent(username)}&month=${formattedMonth}`)
        .then(response => {
            if (!response.ok) throw new Error("Failed to download PDF");
            return response.blob();
        })
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = `Payslip-${username}-${formattedMonth}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        })
        .catch(err => {
            console.error("Error downloading PDF:", err);
            showAlert("Your Timesheet Didn't Approve on This Month.", "danger");
        });
}





function viewBankDetails(employeeName) {
  fetch(`/getBankDetails?Employeename=${encodeURIComponent(employeeName)}`)
    .then(response => response.json())
    .then(data => {
      if (data.accountHolder) {
        // Set modal title and fields
        document.getElementById("bankDetailsModalTitle").innerText = `Bank Details - ${employeeName}`;
        document.getElementById("modalAccountHolder").innerText = data.accountHolder || "-";
        document.getElementById("modalAccountNumber").innerText = data.accountNumber || "-";
        document.getElementById("modalIFSC").innerText = data.ifsc || "-";
        document.getElementById("modalBankName").innerText = data.bankName || "-";

        // Show photo if exists
        const bankPhoto = document.getElementById("modalBankPhoto");
        if (data.bankBookPhotoPath) {
          const fullImageUrl = data.bankBookPhotoPath.replace(/^[:]?/, '').replace(/\\/g, '/');
          bankPhoto.src = fullImageUrl;
          bankPhoto.style.display = "block";
		  
		  // 🆕 Open image in new tab on click
		    bankPhoto.style.cursor = "pointer";
		    bankPhoto.onclick = () => {
		      window.open(fullImageUrl, "_blank");
		    };
        } else {
          bankPhoto.style.display = "none";
        }

        // Add copy + tooltip logic
        const fields = ["modalAccountHolder", "modalAccountNumber", "modalIFSC", "modalBankName"];
        fields.forEach(fieldId => {
          const icon = document.querySelector(`#${fieldId} + i`);
          if (icon) {
            icon.className = "bi bi-clipboard ms-2";
            icon.setAttribute("title", "Copy");
            icon.onclick = () => {
              const text = document.getElementById(fieldId).innerText;
              navigator.clipboard.writeText(text).then(() => {
               icon.className = "bi bi-check-lg text-success ms-2"; 
                icon.setAttribute("title", "Copied!");

                // Revert after 1.5s
                setTimeout(() => {
                  icon.className = "bi bi-clipboard ms-2";
                  icon.setAttribute("title", "Copy");
                }, 1500);
              });
            };
          }
        });

        // Show modal
        const modal = new bootstrap.Modal(document.getElementById("bankDetailsModal"));
        modal.show();
      } else {
		showAlert(`Bank details not found for ${employeeName}!`, "danger");
      }
    })
    .catch(error => {
      console.error("Error fetching bank details:", error);
    });
}




function copyToClipboard(id) {
  const element = document.getElementById(id);
  const icon = element.nextElementSibling; 

  const text = element.innerText;
  navigator.clipboard.writeText(text)
    .then(() => {
      const originalClass = icon.className;
      icon.className = "bi bi-check-lg text-success ms-2"; 
      setTimeout(() => {
        icon.className = originalClass; 
      }, 1500);
    })
    .catch(err => {
      console.error("Copy failed: ", err);
    });
}



function fetchinitialSalary() {
  fetch("/getInitialSalary")
    .then(response => response.json())
    .then(data => {
      const tableBody = document.getElementById("Initialsalary-table-body");
      tableBody.innerHTML = "";

      data.forEach(Salary => {
        const formattedMonthly = "₹ " + Number(Salary["Salary_M"]).toLocaleString('en-IN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });

        const formattedYearly = "₹ " + Number(Salary.yearsalary).toLocaleString('en-IN', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        });

        const bankBtnClass = Salary.bankaccount === "1" ? "btn btn-success" : "btn-outline-secondary";
		
		
		const formatDate = (dateStr) => {
		  if (!dateStr || dateStr === "-") return dateStr;
		  const dateObj = new Date(dateStr);
		  if (isNaN(dateObj)) return dateStr;
		  const day = String(dateObj.getDate()).padStart(2, '0');
		  const month = dateObj.toLocaleString('default', { month: 'short' });
		  const year = dateObj.getFullYear();
		  return `${day}-${month}-${year}`;
		};

		const formattedDOJ = formatDate(Salary["doj"]);
		const formattedEffectiveFrom = formatDate(Salary["effective"]);


        tableBody.innerHTML += `
          <tr>
            <td>${Salary.id}</td>
            <td>${Salary['E-name']}</td>
            <td>${formattedDOJ}</td>
            <td>${formattedMonthly}</td>
            <td>${formattedYearly}</td>
            <td>${formattedEffectiveFrom}</td>
            <td>${Salary.reason}</td>
            <td>
              <button class="btn ${bankBtnClass} btn-sm ms-2" title="View Bank Details" onclick="viewBankDetails('${Salary['E-name']}')">
                View
              </button>
            </td>
			<td>
			  <div class="dropdown">
			    <button class="btn btn-sm btn-success" type="button" data-bs-toggle="dropdown" aria-expanded="false">
			      <i class="bi bi-three-dots-vertical"></i>
			    </button>
			    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0" style="min-width: 220px; border-radius: 12px;">
			      <li>
			        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="openHikeForm('${Salary.id}')">
			          <i class="bi bi-graph-up text-success"></i>
			          <span>Salary Hike</span>
			        </a>
			      </li>
			      <li>
			        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="showSalaryHistory('${Salary['E-name']}')">
			          <i class="bi bi-clock-history text-primary"></i>
			          <span>Salary History</span>
			        </a>
			      </li>
			      <li>
			        <a class="dropdown-item d-flex align-items-center gap-2 py-2" href="#" onclick="opensalaryedit('${Salary.id}')">
			          <i class="bi bi-pencil-square text-warning"></i>
			          <span>Edit Salary</span>
			        </a>
			      </li>
			    </ul>
			  </div>
			</td>
					
          </tr>
        `;
      });
    })
    .catch(error => console.error("Error fetching Charge codes:", error));
}



function opensalaryedit(employeeId) {
    fetch(`/getSalaryById/${employeeId}`)
        .then(res => res.json())
        .then(data => {
            const editFormHTML = `
                <div class="card p-3 mb-3">
                    <h4>Update Employee Salary</h4>
                    <form onsubmit="submitupdatedsalary(event, '${data.id}', '${data['E-name']}', ${data.Salary_M})">
                        ${inputField("Employee Name", "text", "ename", "", data["E-name"], true)}
						${inputField("DOJ", "text", "doj", "", data.doj, true)}
                        ${inputField("Current Salary", "number", "currentSalary", "", data.Salary_M, false, "min='1000'")}
						${inputField("Reason", "text", "reason", "", data.reason, false)}
						${inputField("Effective From", "date", "effective", "", data["effective"], false)}
                        ${formButtons()}
                    </form>
                </div>
            `;
            document.getElementById("form-container").innerHTML = editFormHTML;
        });
}

function submitupdatedsalary(event, id, name) {
    event.preventDefault();

    const updatedSalary = document.querySelector("[name='currentSalary']").value;
	const effectiveFrom = document.querySelector("[name='effective']").value;
	const reason = document.querySelector("[name='reason']").value;

    if (!updatedSalary || updatedSalary < 1000) {
        showAlert("Salary must be at least ₹1000", "danger");
        return;
    }

    const message = `Are you sure you want to update the salary for <strong>${name}</strong>?`;
    document.getElementById('salaryHikeConfirmMessage').innerHTML = message;

    const salaryEditModal = new bootstrap.Modal(document.getElementById('salaryHikeConfirmModal'));
    salaryEditModal.show();

    const confirmBtn = document.getElementById('confirmSalaryHikeBtn');
    confirmBtn.replaceWith(confirmBtn.cloneNode(true)); 
    const newConfirmBtn = document.getElementById('confirmSalaryHikeBtn');

    newConfirmBtn.addEventListener('click', () => {
        showLoader();

        fetch('/updateSalaryByEdit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id,
				name,
                updatedSalary,
				reason,
				effectiveFrom 			
            })
        })
        .then(res => res.text())
        .then(msg => {
            salaryEditModal.hide();
            hideForm();
            showAlert(msg, "success");
            fetchinitialSalary();
        })
        .catch(err => {
            salaryEditModal.hide();
            showAlert("Error: " + err.message, "danger");
        })
        .finally(() => hideLoader());
    });
}


function openHikeForm(employeeId) {
    fetch(`/getSalaryById/${employeeId}`)
        .then(res => res.json())
        .then(data => {
            const hikeFormHTML = `
                <div class="card p-3 mb-3">
                    <h4>Apply Salary Hike</h4>
                    <form onsubmit="submitSalaryHike(event, '${data.id}', '${data['E-name']}', ${data.Salary_M})">
                        ${inputField("Employee Name", "text", "ename", "", data["E-name"], true)}
                        ${inputField("Current Salary", "number", "currentSalary", "", data.Salary_M, true)}
                        ${inputField("Hike Percentage (%)", "number", "hikePercent", "", "", false, "oninput='calculateNewSalaryHike()'")}
                        ${inputField("New Salary", "number", "newSalary", "newSalaryInput", "", true)}
                        ${inputField("Reason", "text", "reason")}
						${inputField("Effective From", "date", "effective")}
                        ${formButtons()}
                    </form>
                </div>
            `;
            document.getElementById("form-container").innerHTML = hikeFormHTML;
        });
}

function calculateNewSalaryHike() {
    const current = parseFloat(document.querySelector("[name='currentSalary']").value);
    const percent = parseFloat(document.querySelector("[name='hikePercent']").value);
    const newSalaryInput = document.querySelector("[name='newSalary']");

    if (!isNaN(current) && !isNaN(percent)) {
        const newSalary = current + (current * percent / 100);
        newSalaryInput.value = newSalary.toFixed(2);
    } else {      
        newSalaryInput.value = "";
    }
}



function submitSalaryHike(event, id, name, currentSalary) {
    event.preventDefault();

    const message = `Are you sure you want to give Salary hike to <strong>${name}</strong>?`;
    document.getElementById('salaryHikeConfirmMessage').innerHTML = message;

    const salaryHikeModal = new bootstrap.Modal(document.getElementById('salaryHikeConfirmModal'));
    salaryHikeModal.show();

    // Remove previous click listeners
    const confirmBtn = document.getElementById('confirmSalaryHikeBtn');
    confirmBtn.replaceWith(confirmBtn.cloneNode(true));
    const newConfirmBtn = document.getElementById('confirmSalaryHikeBtn');

    newConfirmBtn.addEventListener('click', () => {
        showLoader();

        const hikePercent = document.querySelector("[name='hikePercent']").value;
        const newSalary = document.querySelector("[name='newSalary']").value;
        const reason = document.querySelector("[name='reason']").value;
        const effectiveFrom = document.querySelector("[name='effective']").value;

        fetch('/updateSalaryWithHike', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                id,
                name,
                currentSalary,
                newSalary,
                hikePercent,
                reason,
                effectiveFrom   // ⬅️ added this
            })
        })
        .then(res => res.text())
        .then(msg => {
            salaryHikeModal.hide();
            hideForm();
            showAlert(msg, "success");
            fetchinitialSalary();
        })
        .catch(err => {
            salaryHikeModal.hide();
            showAlert('Error: ' + err.message, "danger");
        })
        .finally(() => hideLoader());
    });
}



function showSalaryHistory(empName) {
    document.getElementById("historyEmpName").innerText = empName;

    fetch(`/getSalaryHistory?employeeName=${encodeURIComponent(empName)}`)
        .then(response => response.json())
        .then(history => {
            const tbody = document.getElementById("salaryHistoryTableBody");
            tbody.innerHTML = "";

            if (history.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No salary history found.</td></tr>`;
            } else {
                history.forEach(entry => {
                    const formattedOldSalary = "₹ " + Number(entry.oldsalary).toLocaleString('en-IN', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    });

                    const formattedNewSalary = "₹ " + Number(entry.newsalary).toLocaleString('en-IN', {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2
                    });

                    tbody.innerHTML += `
                        <tr>
                            <td>${entry.effectiveFrom}</td>
                            <td>${formattedOldSalary}</td>
                            <td>${formattedNewSalary}</td>
                            <td>${entry.hikePercent}%</td>
                            <td>${entry.reason}</td>
                        </tr>
                    `;
                });
            }

            // Show modal
            const salaryModal = new bootstrap.Modal(document.getElementById('salaryHistoryModal'));
            salaryModal.show();
        })
        .catch(error => {
            console.error("Error fetching salary history:", error);
            showAlert("Failed to load salary history.", "danger");
        });
}

