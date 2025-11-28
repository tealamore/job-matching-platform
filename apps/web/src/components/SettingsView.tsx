// src/components/SettingsView.tsx
'use client';

import { useState, useEffect } from 'react';
import { getMe, updateMe } from '@/requests/requests';

type UserData = {
    id: string;
    name: string;
    email: string;
    phone: string;
    userType: string;
};

export default function SettingsView({ onBack }: { onBack: () => void }) {
    const [userData, setUserData] = useState<UserData | null>(null);
    const [loading, setLoading] = useState(true);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        password: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');

    useEffect(() => {
        getMe()
            .then((data) => {
                setUserData(data);
                setFormData({
                    name: data.name,
                    email: data.email,
                    phone: data.phone,
                    password: ''
                });
                setLoading(false);
            })
            .catch((err) => {
                console.error("Failed to fetch user data", err);
                setLoading(false);
            });
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setSuccessMessage('');

        try {
            const updateData: any = {
                name: formData.name,
                email: formData.email,
                phone: formData.phone
            };

            if (formData.password) {
                updateData.password = formData.password;
            }

            await updateMe(updateData);

            setSuccessMessage('Profile updated successfully!');
            setFormData({ ...formData, password: '' });

            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (err) {
            console.error("Failed to update profile", err);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-full items-center justify-center">
                <div className="text-white">Loading</div>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl w-full p-4">
            <div className="bg-white/10 backdrop-blur-xl rounded-2xl border border-white/20 shadow-xl">
                <div className="p-6 border-b border-white/10">
                    <div className="flex items-center justify-between">
                        <h2 className="text-2xl font-bold text-white">Account Settings</h2>
                        <button
                            onClick={onBack}
                            className="text-white/80 hover:text-white transition"
                        >
                            ← Back
                        </button>
                    </div>
                    {userData && (
                        <p className="text-white/60 text-sm mt-1">
                            Account Type: {userData.userType === 'JOB_SEEKER' ? 'Job Seeker' : 'Employer'}
                        </p>
                    )}
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-white mb-1">
                            Name *
                        </label>
                        <input
                            type="text"
                            required
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Your name"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-white mb-1">
                            Email *
                        </label>
                        <input
                            type="email"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="your.email@example.com"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-white mb-1">
                            Phone *
                        </label>
                        <input
                            type="tel"
                            required
                            value={formData.phone}
                            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="(555) 123-4567"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-white mb-1">
                            New Password
                        </label>
                        <input
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            className="w-full px-4 py-2 bg-white/10 border border-white/20 rounded-lg text-white placeholder-white/40 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            placeholder="Leave blank to keep current password"
                        />
                        <p className="text-white/50 text-xs mt-1">
                            Only fill this out if you want to change your password
                        </p>
                    </div>

                    {successMessage && (
                        <div className="p-3 bg-green-500/20 border border-green-500/30 rounded-lg text-green-100 text-sm">
                            {successMessage}
                        </div>
                    )}

                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onBack}
                            className="flex-1 px-4 py-2 text-sm font-medium text-white bg-white/10 border border-white/20 rounded-lg hover:bg-white/20 transition"
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Saving' : 'Save'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
