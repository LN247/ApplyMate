/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useMemo } from 'react';
import { 
  onAuthStateChanged, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  User,
  GoogleAuthProvider,
  signInWithPopup
} from 'firebase/auth';
import { 
  collection, 
  doc, 
  setDoc, 
  getDoc, 
  onSnapshot, 
  query, 
  where, 
  orderBy, 
  addDoc, 
  deleteDoc,
  getDocFromServer
} from 'firebase/firestore';
import { auth, db } from './firebase';
import { Application, UserProfile, ApplicationStatus, OperationType, FirestoreErrorInfo, VaultDocument, DiscoveryOpportunity } from './types';
import { 
  Plus, 
  LogOut, 
  Briefcase, 
  Clock, 
  CheckCircle2, 
  XCircle, 
  Calendar, 
  Building2,
  Loader2,
  AlertCircle,
  Trash2,
  Mail,
  Lock,
  User as UserIcon,
  Search,
  Filter,
  ArrowRight,
  ChevronRight,
  LayoutDashboard,
  PlusCircle,
  History,
  Settings,
  Bell,
  MoreVertical,
  ExternalLink
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Helper for Tailwind class merging
function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

// Firebase Error Handling
const handleFirestoreError = (error: unknown, operationType: OperationType, path: string | null) => {
  const errInfo: FirestoreErrorInfo = {
    error: error instanceof Error ? error.message : String(error),
    authInfo: {
      userId: auth.currentUser?.uid,
      email: auth.currentUser?.email,
      emailVerified: auth.currentUser?.emailVerified,
      isAnonymous: auth.currentUser?.isAnonymous,
      tenantId: auth.currentUser?.tenantId,
      providerInfo: auth.currentUser?.providerData.map(provider => ({
        providerId: provider.providerId,
        displayName: provider.displayName,
        email: provider.email,
        photoUrl: provider.photoURL
      })) || []
    },
    operationType,
    path
  };
  console.error('Firestore Error:', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
};

// --- Sub-components ---

const GoogleIcon = () => (
  <svg viewBox="0 0 24 24" className="w-5 h-5" xmlns="http://www.w3.org/2000/svg">
    <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
    <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
    <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05"/>
    <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
  </svg>
);

const ErrorBoundary = ({ error, reset }: { error: string, reset: () => void }) => (
  <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-md flex items-center justify-center z-[100] p-6">
    <motion.div 
      initial={{ scale: 0.9, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      className="bg-white p-10 rounded-[3rem] max-w-md w-full shadow-2xl text-center"
    >
      <div className="w-20 h-20 bg-rose-50 text-rose-600 rounded-[2rem] flex items-center justify-center mx-auto mb-6">
        <AlertCircle className="w-10 h-10" />
      </div>
      <h2 className="text-2xl font-black text-slate-900 mb-3">System Error</h2>
      <p className="text-slate-500 text-sm mb-8 leading-relaxed">We encountered a problem with the database connection. Please try again.</p>
      <button onClick={reset} className="m3-button-primary w-full h-14">
        Restart Application
      </button>
    </motion.div>
  </div>
);

const LoadingScreen = () => (
  <div className="fixed inset-0 bg-slate-50 flex flex-col items-center justify-center z-[100]">
    <motion.div 
      animate={{ rotate: 360 }}
      transition={{ duration: 1.5, repeat: Infinity, ease: "linear" }}
      className="relative"
    >
      <div className="w-16 h-16 border-4 border-indigo-100 rounded-full" />
      <div className="w-16 h-16 border-4 border-indigo-600 rounded-full border-t-transparent absolute inset-0" />
    </motion.div>
    <p className="mt-8 text-slate-400 font-black uppercase tracking-[0.2em] text-[10px]">Initializing ApplyMate</p>
  </div>
);

// --- Main Application ---

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [authMode, setAuthMode] = useState<'login' | 'signup'>('login');
  const [error, setError] = useState<string | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [documents, setDocuments] = useState<VaultDocument[]>([]);
  const [discovered, setDiscovered] = useState<DiscoveryOpportunity[]>([]);
  const [preferences, setPreferences] = useState<any>(null);
  const [filter, setFilter] = useState<ApplicationStatus | 'All'>('All');
  const [showAddModal, setShowAddModal] = useState(false);
  const [showAutofillSheet, setShowAutofillSheet] = useState(false);
  const [selectedOpportunity, setSelectedOpportunity] = useState<DiscoveryOpportunity | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState('Dashboard');

  // Mock Discovery Data
  useEffect(() => {
    if (discovered.length === 0) {
      setDiscovered([
        {
          id: '1',
          title: 'AI Research Intern',
          organization: 'Google DeepMind',
          location: 'London, UK',
          matchScore: 98,
          description: 'Join the team pushing the boundaries of artificial intelligence. Focus on large language models and reinforcement learning.',
          link: 'https://google.com/careers'
        },
        {
          id: '2',
          title: 'Frontend Developer (Scholarship)',
          organization: 'Vercel',
          location: 'Remote',
          matchScore: 85,
          description: 'A scholarship program for aspiring frontend engineers. Learn Next.js and React from the creators.',
          link: 'https://vercel.com'
        }
      ]);
    }
  }, [discovered]);

  // Auth Listener
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      setUser(firebaseUser);
      if (firebaseUser) {
        try {
          const docRef = doc(db, 'users', firebaseUser.uid);
          const docSnap = await getDoc(docRef);
          if (docSnap.exists()) {
            setProfile(docSnap.data() as UserProfile);
          } else {
            const newProfile: UserProfile = {
              uid: firebaseUser.uid,
              fullName: firebaseUser.displayName || 'New User',
              email: firebaseUser.email || '',
              createdAt: new Date().toISOString()
            };
            await setDoc(docRef, newProfile);
            setProfile(newProfile);
          }
        } catch (err) {
          console.error("Profile sync error:", err);
        }
      } else {
        setProfile(null);
      }
      setLoading(false);
    });
    return unsubscribe;
  }, []);

  // Firestore Connection Test
  useEffect(() => {
    const testConnection = async () => {
      try {
        await getDocFromServer(doc(db, 'test', 'connection'));
      } catch (err) {
        if (err instanceof Error && err.message.includes('offline')) {
          console.warn("Firestore is currently offline.");
        }
      }
    };
    testConnection();
  }, []);

  // Real-time Data Subscription
  useEffect(() => {
    if (!user) {
      setApplications([]);
      return;
    }

    const q = query(
      collection(db, 'applications'),
      where('userId', '==', user.uid),
      orderBy('createdAt', 'desc')
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const apps = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Application));
      setApplications(apps);
    }, (err) => {
      handleFirestoreError(err, OperationType.LIST, 'applications');
    });

    // Documents Listener
    const docsUnsubscribe = onSnapshot(collection(db, 'users', user.uid, 'documents'), (snapshot) => {
      const docs = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as VaultDocument));
      setDocuments(docs);
    });

    // Preferences Listener
    const prefsUnsubscribe = onSnapshot(doc(db, 'users', user.uid, 'preferences', 'profile'), (snapshot) => {
      if (snapshot.exists()) {
        setPreferences(snapshot.data());
      }
    });

    return () => {
      unsubscribe();
      docsUnsubscribe();
      prefsUnsubscribe();
    };
  }, [user]);

  // Handlers
  const handleAuth = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    const formData = new FormData(e.currentTarget);
    const email = formData.get('email') as string;
    const password = formData.get('password') as string;
    const fullName = formData.get('fullName') as string;

    try {
      if (authMode === 'signup') {
        const res = await createUserWithEmailAndPassword(auth, email, password);
        const newProfile: UserProfile = {
          uid: res.user.uid,
          fullName,
          email,
          createdAt: new Date().toISOString()
        };
        await setDoc(doc(db, 'users', res.user.uid), newProfile);
        setProfile(newProfile);
      } else {
        await signInWithEmailAndPassword(auth, email, password);
      }
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleGoogleLogin = async () => {
    const provider = new GoogleAuthProvider();
    try {
      await signInWithPopup(auth, provider);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleAddApplication = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!user) return;

    const formData = new FormData(e.currentTarget);
    const docRef = doc(collection(db, 'applications'));
    const newApp = {
      id: docRef.id,
      userId: user.uid,
      title: formData.get('title') as string,
      organization: formData.get('organization') as string,
      status: formData.get('status') as ApplicationStatus,
      deadline: formData.get('deadline') as string,
      createdAt: new Date().toISOString()
    };

    try {
      await setDoc(docRef, newApp);
      setShowAddModal(false);
    } catch (err) {
      handleFirestoreError(err, OperationType.CREATE, 'applications');
    }
  };

  const deleteApplication = async (id: string) => {
    if (!confirm('Are you sure you want to remove this opportunity?')) return;
    try {
      await deleteDoc(doc(db, 'applications', id));
    } catch (err) {
      handleFirestoreError(err, OperationType.DELETE, `applications/${id}`);
    }
  };

  // Memoized Data
  const filteredApps = useMemo(() => {
    return applications.filter(app => {
      const matchesFilter = filter === 'All' || app.status === filter;
      const matchesSearch = app.title.toLowerCase().includes(searchQuery.toLowerCase()) || 
                           app.organization.toLowerCase().includes(searchQuery.toLowerCase());
      return matchesFilter && matchesSearch;
    });
  }, [applications, filter, searchQuery]);

  const stats = useMemo(() => ({
    total: applications.length,
    pending: applications.filter(a => a.status === 'Pending').length,
    accepted: applications.filter(a => a.status === 'Accepted').length,
    rejected: applications.filter(a => a.status === 'Rejected').length,
  }), [applications]);

  if (loading) return <LoadingScreen />;
  if (error && error.startsWith('{')) return <ErrorBoundary error={error} reset={() => setError(null)} />;

  // --- Auth View ---
  if (!user) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6 selection:bg-indigo-100">
        <motion.div 
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white p-12 rounded-[3.5rem] w-full max-w-lg shadow-2xl border border-slate-100 relative overflow-hidden"
        >
          {/* Decorative background element */}
          <div className="absolute -top-24 -right-24 w-64 h-64 bg-indigo-50 rounded-full blur-3xl opacity-50" />
          
          <div className="relative z-10">
            <div className="flex flex-col items-center mb-12">
              <motion.div 
                whileHover={{ scale: 1.05, rotate: 5 }}
                className="w-24 h-24 bg-indigo-600 rounded-[2.5rem] flex items-center justify-center shadow-2xl shadow-indigo-200 mb-8"
              >
                <Briefcase className="w-12 h-12 text-white" />
              </motion.div>
              <h1 className="text-4xl font-black text-slate-900 tracking-tight text-center">ApplyMate</h1>
              <p className="text-slate-400 mt-3 font-bold uppercase tracking-widest text-[10px]">Professional Career Management</p>
            </div>

            <div className="space-y-6">
              <button 
                onClick={handleGoogleLogin}
                className="w-full h-16 flex items-center justify-center gap-4 bg-white border-2 border-slate-100 rounded-2xl font-black text-slate-700 hover:bg-slate-50 hover:border-slate-200 transition-all active:scale-[0.98]"
              >
                <GoogleIcon />
                Continue with Google
              </button>

              <div className="flex items-center gap-6 py-2">
                <div className="h-[2px] flex-1 bg-slate-100" />
                <span className="text-[10px] uppercase tracking-[0.3em] font-black text-slate-300">or</span>
                <div className="h-[2px] flex-1 bg-slate-100" />
              </div>

              <form onSubmit={handleAuth} className="space-y-4">
                {authMode === 'signup' && (
                  <div className="relative group">
                    <UserIcon className="absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-indigo-500 transition-colors" />
                    <input name="fullName" required className="m3-input pl-16" placeholder="Your Full Name" />
                  </div>
                )}
                <div className="relative group">
                  <Mail className="absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-indigo-500 transition-colors" />
                  <input name="email" type="email" required className="m3-input pl-16" placeholder="Email Address" />
                </div>
                <div className="relative group">
                  <Lock className="absolute left-6 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-300 group-focus-within:text-indigo-500 transition-colors" />
                  <input name="password" type="password" required className="m3-input pl-16" placeholder="Secure Password" />
                </div>

                {error && (
                  <motion.div 
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="p-5 bg-rose-50 text-rose-600 text-xs font-black rounded-2xl flex items-center gap-4 border border-rose-100"
                  >
                    <AlertCircle className="w-6 h-6 shrink-0" />
                    {error}
                  </motion.div>
                )}

                <button type="submit" className="m3-button-primary w-full h-16 text-lg shadow-2xl shadow-indigo-200 mt-4">
                  {authMode === 'login' ? 'Sign In' : 'Create Account'}
                  <ArrowRight className="w-6 h-6" />
                </button>
              </form>
            </div>

            <div className="mt-10 text-center">
              <button 
                onClick={() => {
                  setAuthMode(authMode === 'login' ? 'signup' : 'login');
                  setError(null);
                }}
                className="text-sm font-black text-indigo-600 hover:text-indigo-800 transition-colors uppercase tracking-widest"
              >
                {authMode === 'login' ? "New here? Create account" : "Have an account? Log in"}
              </button>
            </div>
          </div>
        </motion.div>
      </div>
    );
  }

  // --- Main View ---
  return (
    <div className="min-h-screen bg-slate-50 flex selection:bg-indigo-100">
      {/* Sidebar - Desktop */}
      <aside className="hidden lg:flex w-80 bg-white border-r border-slate-200 flex-col p-8 sticky top-0 h-screen">
        <div className="flex items-center gap-4 mb-16 px-2">
          <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-xl shadow-indigo-100">
            <Briefcase className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tighter">ApplyMate</h1>
        </div>

        <nav className="space-y-3 flex-1">
          {[
            { icon: LayoutDashboard, label: 'Dashboard' },
            { icon: Search, label: 'Discovery' },
            { icon: Lock, label: 'Vault' },
            { icon: History, label: 'History' },
            { icon: Settings, label: 'Settings' },
          ].map((item) => (
            <button 
              key={item.label}
              onClick={() => setActiveTab(item.label)}
              className={cn(
                "w-full flex items-center gap-4 px-6 py-4 rounded-2xl font-black text-sm transition-all group",
                activeTab === item.label 
                  ? "bg-indigo-600 text-white shadow-xl shadow-indigo-100" 
                  : "text-slate-400 hover:bg-slate-50 hover:text-slate-900"
              )}
            >
              <item.icon className={cn("w-6 h-6 transition-transform group-hover:scale-110", activeTab === item.label ? "text-white" : "text-slate-300")} />
              {item.label}
            </button>
          ))}
        </nav>

        <div className="pt-8 border-t border-slate-100">
          <div className="bg-slate-50 p-6 rounded-[2rem] mb-8">
            <div className="flex items-center gap-4 mb-4">
              <div className="w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center text-white font-black text-lg shadow-lg">
                {profile?.fullName?.[0]}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-black text-slate-900 truncate">{profile?.fullName}</p>
                <p className="text-[10px] text-indigo-600 font-black uppercase tracking-widest">Premium User</p>
              </div>
            </div>
            <div className="h-2 bg-slate-200 rounded-full overflow-hidden">
              <div className="h-full bg-indigo-600 w-3/4 rounded-full" />
            </div>
            <p className="text-[10px] text-slate-400 font-bold mt-2">75% of goal reached</p>
          </div>
          
          <button 
            onClick={() => signOut(auth)}
            className="w-full flex items-center gap-4 px-6 py-4 rounded-2xl font-black text-sm text-rose-600 hover:bg-rose-50 transition-all"
          >
            <LogOut className="w-6 h-6" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Mobile Header */}
        <header className="bg-white/90 backdrop-blur-xl sticky top-0 z-30 border-b border-slate-200 lg:hidden px-6 h-24 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-lg">
              <Briefcase className="w-7 h-7 text-white" />
            </div>
            <h1 className="text-2xl font-black text-slate-900 tracking-tighter">ApplyMate</h1>
          </div>
          <button 
            onClick={() => signOut(auth)}
            className="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-600 hover:bg-rose-50 hover:text-rose-600 transition-colors"
          >
            <LogOut className="w-6 h-6" />
          </button>
        </header>

        <main className="flex-1 max-w-7xl mx-auto w-full px-6 lg:px-12 py-12">
          {activeTab === 'Dashboard' && (
            <>
              {/* Dashboard Header */}
              <div className="flex flex-col md:flex-row md:items-end justify-between gap-8 mb-16">
                <div>
                  <motion.h2 
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    className="text-5xl font-black text-slate-900 tracking-tight leading-none"
                  >
                    Hello, {profile?.fullName?.split(' ')[0]}!
                  </motion.h2>
                  <p className="text-slate-400 font-bold text-lg mt-4 flex items-center gap-3">
                    <Clock className="w-6 h-6 text-indigo-500" />
                    You have <span className="text-indigo-600">{stats.pending}</span> active applications.
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <button className="w-16 h-16 rounded-[1.5rem] bg-white border-2 border-slate-100 flex items-center justify-center text-slate-400 hover:text-indigo-600 hover:border-indigo-100 transition-all shadow-sm">
                    <Bell className="w-7 h-7" />
                  </button>
                  <button 
                    onClick={() => setShowAddModal(true)}
                    className="m3-button-primary h-16 px-10 text-lg shadow-2xl shadow-indigo-100"
                  >
                    <PlusCircle className="w-6 h-6" />
                    Add Opportunity
                  </button>
                </div>
              </div>

              {/* Stats Bento Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 mb-16">
                {[
                  { label: 'Total', value: stats.total, icon: Briefcase, color: 'text-indigo-600', bg: 'bg-indigo-50', border: 'border-indigo-100' },
                  { label: 'Pending', value: stats.pending, icon: Clock, color: 'text-blue-600', bg: 'bg-blue-50', border: 'border-blue-100' },
                  { label: 'Accepted', value: stats.accepted, icon: CheckCircle2, color: 'text-emerald-600', bg: 'bg-emerald-50', border: 'border-emerald-100' },
                  { label: 'Rejected', value: stats.rejected, icon: XCircle, color: 'text-rose-600', bg: 'bg-rose-50', border: 'border-rose-100' },
                ].map((stat, i) => (
                  <motion.div 
                    key={stat.label}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.1 }}
                    className={cn("m3-card p-8 group cursor-default", stat.border)}
                  >
                    <div className={cn("w-16 h-16 rounded-[1.5rem] flex items-center justify-center mb-8 transition-transform group-hover:scale-110 group-hover:rotate-3", stat.bg, stat.color)}>
                      <stat.icon className="w-8 h-8" />
                    </div>
                    <p className="text-5xl font-black text-slate-900 tracking-tighter">{stat.value}</p>
                    <p className="text-xs text-slate-400 font-black uppercase tracking-[0.2em] mt-3">{stat.label} Applications</p>
                  </motion.div>
                ))}
              </div>

              {/* Controls Bar */}
              <div className="bg-white p-6 rounded-[2.5rem] border border-slate-200 mb-12 flex flex-col xl:flex-row gap-6 items-center shadow-sm">
                <div className="relative flex-1 w-full group">
                  <Search className="absolute left-6 top-1/2 -translate-y-1/2 w-6 h-6 text-slate-300 group-focus-within:text-indigo-500 transition-colors" />
                  <input 
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-16 pr-6 h-16 bg-slate-50 border-2 border-transparent rounded-2xl text-sm font-bold outline-none transition-all focus:bg-white focus:border-indigo-100 focus:ring-8 focus:ring-indigo-500/5 placeholder:text-slate-400" 
                    placeholder="Search by role, company, or location..." 
                  />
                </div>
                <div className="flex gap-3 p-2 bg-slate-50 rounded-[1.5rem] w-full xl:w-auto overflow-x-auto no-scrollbar">
                  {['All', 'Pending', 'Accepted', 'Rejected'].map((f) => (
                    <button
                      key={f}
                      onClick={() => setFilter(f as any)}
                      className={cn(
                        "px-8 py-3.5 text-[10px] font-black uppercase tracking-widest rounded-xl transition-all whitespace-nowrap",
                        filter === f 
                          ? "bg-white text-indigo-600 shadow-xl shadow-indigo-500/10" 
                          : "text-slate-400 hover:text-slate-600"
                      )}
                    >
                      {f}
                    </button>
                  ))}
                </div>
              </div>

              {/* Applications List */}
              <div className="space-y-6">
                <AnimatePresence mode="popLayout">
                  {filteredApps.length > 0 ? (
                    filteredApps.map((app) => (
                      <motion.div
                        key={app.id}
                        layout
                        initial={{ opacity: 0, scale: 0.98 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        className="m3-card p-8 flex flex-col md:flex-row md:items-center justify-between gap-8 group hover:border-indigo-200 hover:shadow-2xl hover:shadow-indigo-500/5"
                      >
                        <div className="flex items-center gap-8">
                          <div className={cn(
                            "w-20 h-20 rounded-[2rem] flex items-center justify-center text-2xl shadow-inner",
                            app.status === 'Accepted' ? "bg-emerald-50 text-emerald-600" :
                            app.status === 'Rejected' ? "bg-rose-50 text-rose-600" :
                            "bg-blue-50 text-blue-600"
                          )}>
                            {app.status === 'Accepted' ? <CheckCircle2 className="w-10 h-10" /> :
                             app.status === 'Rejected' ? <XCircle className="w-10 h-10" /> : 
                             <Clock className="w-10 h-10" />}
                          </div>
                          <div>
                            <h3 className="text-2xl font-black text-slate-900 tracking-tight group-hover:text-indigo-600 transition-colors">{app.title}</h3>
                            <div className="flex flex-wrap items-center gap-x-8 gap-y-3 mt-3">
                              <span className="flex items-center gap-3 text-sm text-slate-500 font-bold">
                                <Building2 className="w-5 h-5 text-slate-300" /> {app.organization}
                              </span>
                              <span className="flex items-center gap-3 text-sm text-slate-500 font-bold">
                                <Calendar className="w-5 h-5 text-slate-300" /> {new Date(app.deadline).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                              </span>
                            </div>
                          </div>
                        </div>
                        
                        <div className="flex items-center justify-between md:justify-end gap-8 pt-6 md:pt-0 border-t md:border-none border-slate-100">
                          <span className={cn(
                            "m3-chip px-6 py-2.5",
                            app.status === 'Accepted' ? "status-accepted" :
                            app.status === 'Rejected' ? "status-rejected" :
                            "status-pending"
                          )}>
                            <div className="w-2 h-2 rounded-full bg-current" />
                            {app.status}
                          </span>
                          <div className="flex items-center gap-3">
                            <button className="w-14 h-14 rounded-2xl bg-slate-50 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-all flex items-center justify-center">
                              <ExternalLink className="w-6 h-6" />
                            </button>
                            <button 
                              onClick={() => deleteApplication(app.id)}
                              className="w-14 h-14 rounded-2xl bg-slate-50 text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-all flex items-center justify-center"
                            >
                              <Trash2 className="w-6 h-6" />
                            </button>
                          </div>
                        </div>
                      </motion.div>
                    ))
                  ) : (
                    <motion.div 
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      className="text-center py-40 bg-white rounded-[4rem] border-4 border-dashed border-slate-100"
                    >
                      <div className="w-32 h-32 bg-slate-50 rounded-[3rem] flex items-center justify-center mx-auto mb-8">
                        <Search className="w-16 h-16 text-slate-200" />
                      </div>
                      <h3 className="text-3xl font-black text-slate-900 tracking-tight">No opportunities found</h3>
                      <p className="text-slate-400 font-bold mt-3 text-lg">Try adjusting your filters or start fresh.</p>
                      <button 
                        onClick={() => setShowAddModal(true)}
                        className="mt-12 m3-button-primary h-16 px-12 mx-auto"
                      >
                        Add New Opportunity
                      </button>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </>
          )}

          {activeTab === 'Discovery' && (
            <div className="space-y-12">
              <div className="flex justify-between items-end">
                <div>
                  <h2 className="text-5xl font-black text-slate-900 tracking-tight">Discovery Engine</h2>
                  <p className="text-slate-400 font-bold text-lg mt-4">AI-powered opportunities tailored for you.</p>
                </div>
                <button 
                  onClick={() => setActiveTab('Settings')}
                  className="m3-button-secondary h-14 px-8"
                >
                  <Filter className="w-5 h-5" />
                  Refine Search
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {discovered.length > 0 ? discovered.map(opp => (
                  <motion.div 
                    key={opp.id}
                    className="m3-card p-8 group"
                  >
                    <div className="flex justify-between items-start mb-6">
                      <div className="w-16 h-16 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600 font-black text-xl">
                        {opp.matchScore}%
                      </div>
                      <button 
                        onClick={() => {
                          setSelectedOpportunity(opp);
                          setShowAutofillSheet(true);
                        }}
                        className="m3-button-primary h-12 px-6 text-sm"
                      >
                        Apply Now
                      </button>
                    </div>
                    <h3 className="text-2xl font-black text-slate-900 mb-2">{opp.title}</h3>
                    <p className="text-slate-500 font-bold mb-6 flex items-center gap-2">
                      <Building2 className="w-5 h-5" /> {opp.organization} • {opp.location}
                    </p>
                    <p className="text-slate-400 text-sm leading-relaxed line-clamp-3">{opp.description}</p>
                  </motion.div>
                )) : (
                  <div className="col-span-full py-32 text-center bg-white rounded-[3rem] border-4 border-dashed border-slate-100">
                    <Search className="w-16 h-16 text-slate-200 mx-auto mb-6" />
                    <h3 className="text-2xl font-black text-slate-900">Scanning for opportunities...</h3>
                    <p className="text-slate-400 font-bold mt-2">Make sure your preferences are set in Settings.</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'Vault' && (
            <div className="space-y-12">
              <div className="flex justify-between items-end">
                <div>
                  <h2 className="text-5xl font-black text-slate-900 tracking-tight">Document Vault</h2>
                  <p className="text-slate-400 font-bold text-lg mt-4">Secure storage for your application assets.</p>
                </div>
                <button className="m3-button-primary h-14 px-8">
                  <Plus className="w-5 h-5" />
                  Upload Document
                </button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8">
                {documents.map(doc => (
                  <div key={doc.id} className="m3-card p-6 flex items-center gap-6">
                    <div className="w-14 h-14 bg-indigo-50 rounded-2xl flex items-center justify-center text-indigo-600">
                      <LayoutDashboard className="w-7 h-7" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-black text-slate-900 truncate">{doc.name}</p>
                      <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mt-1">{doc.category}</p>
                    </div>
                    <button className="text-slate-300 hover:text-indigo-600 transition-colors">
                      <ExternalLink className="w-5 h-5" />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'History' && (
            <div className="space-y-12">
              <h2 className="text-5xl font-black text-slate-900 tracking-tight">Application History</h2>
              <div className="space-y-6">
                {applications.filter(a => a.status !== 'Pending').map(app => (
                  <div key={app.id} className="m3-card p-8 flex items-center justify-between">
                    <div className="flex items-center gap-8">
                      <div className={cn(
                        "w-16 h-16 rounded-2xl flex items-center justify-center",
                        app.status === 'Accepted' ? "bg-emerald-50 text-emerald-600" : "bg-rose-50 text-rose-600"
                      )}>
                        {app.status === 'Accepted' ? <CheckCircle2 className="w-8 h-8" /> : <XCircle className="w-8 h-8" />}
                      </div>
                      <div>
                        <h3 className="text-xl font-black text-slate-900">{app.title}</h3>
                        <p className="text-slate-400 font-bold">{app.organization}</p>
                      </div>
                    </div>
                    <span className={cn(
                      "m3-chip px-6 py-2",
                      app.status === 'Accepted' ? "status-accepted" : "status-rejected"
                    )}>
                      {app.status}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'Settings' && (
            <div className="max-w-2xl space-y-12">
              <h2 className="text-5xl font-black text-slate-900 tracking-tight">Settings</h2>
              
              <section className="space-y-8">
                <h3 className="text-xs font-black text-indigo-600 uppercase tracking-[0.3em]">Discovery Preferences</h3>
                <div className="space-y-6">
                  <div className="group">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-3 block">Field of Study</label>
                    <input className="m3-input h-14" defaultValue={preferences?.fieldOfStudy || "Computer Science"} />
                  </div>
                  <div className="group">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-3 block">Preferred Locations</label>
                    <input className="m3-input h-14" defaultValue={preferences?.preferredLocations || "Remote, Europe"} />
                  </div>
                </div>
              </section>

              <section className="space-y-8 pt-8 border-t border-slate-100">
                <h3 className="text-xs font-black text-indigo-600 uppercase tracking-[0.3em]">Account</h3>
                <button 
                  onClick={() => signOut(auth)}
                  className="w-full h-16 bg-rose-50 text-rose-600 rounded-2xl font-black flex items-center justify-center gap-4 hover:bg-rose-100 transition-colors"
                >
                  <LogOut className="w-6 h-6" />
                  Sign Out of ApplyMate
                </button>
              </section>
            </div>
          )}
        </main>
      </div>

      {/* Add Opportunity Modal */}
      <AnimatePresence>
        {showAddModal && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xl flex items-center justify-center z-[100] p-6">
            <motion.div 
              initial={{ scale: 0.9, opacity: 0, y: 40 }}
              animate={{ scale: 1, opacity: 1, y: 0 }}
              exit={{ scale: 0.9, opacity: 0, y: 40 }}
              className="bg-white p-12 rounded-[4rem] w-full max-w-2xl shadow-[0_32px_64px_-12px_rgba(0,0,0,0.2)] border border-slate-100 relative overflow-hidden"
            >
              {/* Decorative element */}
              <div className="absolute top-0 right-0 w-40 h-40 bg-indigo-50 rounded-bl-[4rem] -z-10" />
              
              <div className="flex justify-between items-start mb-12">
                <div>
                  <h2 className="text-4xl font-black text-slate-900 tracking-tight">New Opportunity</h2>
                  <p className="text-slate-400 font-bold mt-2">Track your next big career move.</p>
                </div>
                <button 
                  onClick={() => setShowAddModal(false)} 
                  className="w-14 h-14 rounded-2xl bg-slate-100 text-slate-400 hover:text-slate-600 hover:bg-slate-200 transition-all flex items-center justify-center"
                >
                  <XCircle className="w-7 h-7" />
                </button>
              </div>

              <form onSubmit={handleAddApplication} className="space-y-10">
                <div className="space-y-8">
                  <div className="group">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em] mb-4 block group-focus-within:text-indigo-500 transition-colors">Position Title</label>
                    <input name="title" required className="m3-input h-16" placeholder="e.g. Senior Software Engineer" />
                  </div>
                  <div className="group">
                    <label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em] mb-4 block group-focus-within:text-indigo-500 transition-colors">Company / Organization</label>
                    <input name="organization" required className="m3-input h-16" placeholder="e.g. Google, Inc." />
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
                    <div className="group">
                      <label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em] mb-4 block group-focus-within:text-indigo-500 transition-colors">Current Status</label>
                      <div className="relative">
                        <select name="status" className="m3-input h-16 appearance-none pr-12">
                          <option value="Pending">Pending</option>
                          <option value="Accepted">Accepted</option>
                          <option value="Rejected">Rejected</option>
                        </select>
                        <ChevronRight className="absolute right-6 top-1/2 -translate-y-1/2 w-6 h-6 text-slate-300 pointer-events-none rotate-90" />
                      </div>
                    </div>
                    <div className="group">
                      <label className="text-[10px] font-black text-slate-400 uppercase tracking-[0.3em] mb-4 block group-focus-within:text-indigo-500 transition-colors">Deadline Date</label>
                      <input name="deadline" type="date" required className="m3-input h-16" />
                    </div>
                  </div>
                </div>
                
                <div className="flex flex-col sm:flex-row gap-4 pt-6">
                  <button type="submit" className="m3-button-primary flex-1 h-16 text-lg shadow-2xl shadow-indigo-100">
                    Save Opportunity
                    <ArrowRight className="w-6 h-6" />
                  </button>
                  <button 
                    type="button" 
                    onClick={() => setShowAddModal(false)}
                    className="m3-button-secondary px-12 h-16 text-lg"
                  >
                    Discard
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Autofill Assistant Sheet */}
      <AnimatePresence>
        {showAutofillSheet && selectedOpportunity && (
          <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-md z-[110] flex items-end justify-center">
            <motion.div 
              initial={{ y: "100%" }}
              animate={{ y: 0 }}
              exit={{ y: "100%" }}
              className="bg-white w-full max-w-2xl rounded-t-[3rem] p-10 shadow-2xl border-t border-slate-100"
            >
              <div className="w-12 h-1.5 bg-slate-200 rounded-full mx-auto mb-8" />
              
              <div className="flex items-center gap-6 mb-8">
                <div className="w-16 h-16 bg-indigo-600 rounded-2xl flex items-center justify-center text-white">
                  <PlusCircle className="w-8 h-8" />
                </div>
                <div>
                  <h3 className="text-2xl font-black text-slate-900">Autofill Assistant</h3>
                  <p className="text-slate-400 font-bold">ApplyMate Co-pilot is ready.</p>
                </div>
              </div>

              <div className="bg-slate-50 p-6 rounded-2xl mb-8 space-y-4">
                <p className="text-sm text-slate-600 font-bold">We found matching data in your profile:</p>
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-white rounded-xl border border-slate-100">
                    <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mb-1">Full Name</p>
                    <p className="text-sm font-black text-slate-900">{profile?.fullName}</p>
                  </div>
                  <div className="p-4 bg-white rounded-xl border border-slate-100">
                    <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mb-1">Email</p>
                    <p className="text-sm font-black text-slate-900">{profile?.email}</p>
                  </div>
                  <div className="p-4 bg-white rounded-xl border border-slate-100">
                    <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mb-1">Resume</p>
                    <p className="text-sm font-black text-indigo-600">Latest_Resume.pdf</p>
                  </div>
                  <div className="p-4 bg-white rounded-xl border border-slate-100">
                    <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest mb-1">LinkedIn</p>
                    <p className="text-sm font-black text-indigo-600">linkedin.com/in/...</p>
                  </div>
                </div>
              </div>

              <div className="flex gap-4">
                <button 
                  onClick={() => {
                    window.open(selectedOpportunity.link, '_blank');
                    setShowAutofillSheet(false);
                  }}
                  className="m3-button-primary flex-1 h-16 text-lg"
                >
                  Fill This Form
                  <ExternalLink className="w-6 h-6" />
                </button>
                <button 
                  onClick={() => setShowAutofillSheet(false)}
                  className="m3-button-secondary px-10 h-16 text-lg"
                >
                  Cancel
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
