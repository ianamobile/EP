import { RegistrationForm } from "./registration-form";

export class CompanyInfoForm extends RegistrationForm {
    public accountNumber: string;
    public remarks: string;
    public iaId: number;
    public contactInfoId: number;
    public contactAddressId: number;
}

export class ChangePasswordForm {
    public newPassword: string;
    public confirmPassword: string;
}
